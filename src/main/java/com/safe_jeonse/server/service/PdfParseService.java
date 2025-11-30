package com.safe_jeonse.server.service;

import com.safe_jeonse.server.dto.response.ParseResultDto;
import com.safe_jeonse.server.exception.FileParseException;
import com.safe_jeonse.server.exception.NotRegistryDocumentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfParseService {

    private final Tika tika;

    // ===== Common patterns =====
    private static final Pattern TITLE_P   = Pattern.compile("【\\s*([^】]+?)\\s*】");
    private static final Pattern DATE_KR_P = Pattern.compile("(\\d{4})년\\s*(\\d{1,2})월\\s*(\\d{1,2})일");

    // ===== Fuzzy Korean tokens (allow inner spaces) =====
    private static final String K_OWN_TRANSFER = "소\\s*유\\s*권\\s*이\\s*전";
    private static final String K_OWNER        = "소\\s*유\\s*자";
    private static final String K_LIEN         = "근\\s*저\\s*당\\s*권\\s*설\\s*정";
    private static final String K_LIEN_HOLDER  = "근\\s*저\\s*당\\s*권\\s*자";
    private static final String K_DEBTOR       = "채\\s*무\\s*자";
    private static final String K_MAXAMT       = "채\\s*권\\s*최\\s*고\\s*액";
    private static final String K_TERMINATE1   = "말\\s*소";
    private static final String K_TERMINATE2   = "해\\s*지";
    private static final String K_JEHO         = "제\\s*\\d+\\s*호";   // "제 xxxx 호"
    private static final String K_RIGHT_LABEL  = "소\\s*유\\s*권\\s*대\\s*지\\s*권";

    // ===== [GENERIC] Building types & Land categories (폭넓은 커버) =====
    private static final String BUILDING_TYPES =
            "아파트|오피스텔|상가|빌딩|빌라|연립|다세대|주택|주상복합|타워|몰|센터|스퀘어|타운|아이파크|자이|래미안|푸르지오|힐스테이트|롯데캐슬|e\\s*편한세상|더\\s*샵|리버|포레";

    private static final String LAND_CATEGORIES =
            "대|전|답|임야|대지|도로|잡종지|학교용지|공장용지|창고용지|철도용지|유원지|체육용지|주차장|하천|구거|제방|유지|수도용지|양어장";

    // Entry headers (do NOT use \b on Korean)
    private static final Pattern ENTRY_HEAD_GAPGU =
            Pattern.compile("(\\d+)\\s*" + K_OWN_TRANSFER, Pattern.MULTILINE | Pattern.DOTALL);

    // ===== Utils =====
    private static LocalDate parseKoreanDate(String s) {
        Matcher m = DATE_KR_P.matcher(s);
        if (m.find()) {
            int y  = Integer.parseInt(m.group(1));
            int mo = Integer.parseInt(m.group(2));
            int d  = Integer.parseInt(m.group(3));
            return LocalDate.of(y, mo, d);
        }
        throw new IllegalArgumentException("날짜 파싱 실패: " + s);
    }

    private static String findFirstGroup(Pattern p, CharSequence text) {
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1) : null;
    }

    private static String abbreviate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    private static String cleanOwnerTag(String name) {
        if (name == null) return null;
        return name.replaceAll("\\((소유자|공유자)\\)", "").replace(" ", "").trim();
    }

    private static String markInvisibles(String s) {
        if (s == null) return null;
        return s
                .replace("\uFEFF", "⟦BOM⟧")
                .replace("\u200B", "⟦ZWSP⟧")
                .replace("\u200C", "⟦ZWNJ⟧")
                .replace("\u200D", "⟦ZWJ⟧")
                .replace("\u2060", "⟦WJ⟧")
                .replace("\u00A0", "⟦NBSP⟧")
                .replace("\u3000", "⟦IDEOSP⟧");
    }

    private static String safeAbbrev(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max) + "…";
    }

    // Normalize: remove zero-width & unify spaces (keep line breaks)
    private String normalize(String s) {
        if (s == null) return null;
        s = s.replace("\r\n", "\n").replace("\r", "\n");
        // zero-width & BOM
        s = s.replaceAll("[\\uFEFF\\u200C\\u200D\\u2060]", "");
        // unicode spaces -> normal space
        s = s.replaceAll("[\\u00A0\\u1680\\u2000-\\u200B\\u202F\\u205F\\u3000]", " ");
        // tabs/formfeeds
        s = s.replaceAll("[\\t\\f\\x0B]+", " ");
        // collapse multiple spaces & excessive blank lines
        s = s.replaceAll(" +", " ");
        s = s.replaceAll("\\n{3,}", "\n\n");
        return s.trim();
    }

    // ===== Public API =====
    public ParseResultDto parsePdf(MultipartFile file) {
        String originalText = extractTextFromFile(file);
        String normalizedText = normalize(originalText);

        // 등기부등본 문서 검증
        validateRegistryDocument(normalizedText);

        return buildParseResult(normalizedText);
    }

    // 등기부등본 문서 식별 검증
    private void validateRegistryDocument(String text) {
        if (text == null || text.isBlank()) {
            throw new NotRegistryDocumentException("문서 내용이 비어있습니다.");
        }

        // 공백과 제로폭 문자를 제거한 텍스트로 검사
        String cleanText = text.replaceAll("[\\s\\uFEFF\\u200B\\u200C\\u200D\\u2060\\u00A0]+", "");

        if (!cleanText.contains("등기사항전부증명서")) {
            log.warn("등기부등본 식별 실패 - '등기사항전부증명서' 텍스트 미검출");
            throw new NotRegistryDocumentException("등기사항전부증명서가 아닙니다. 올바른 등기부등본 파일을 업로드해주세요.");
        }
    }

    // ===== I/O =====
    private String extractTextFromFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        try (InputStream stream = file.getInputStream()) {
            return tika.parseToString(stream);
        } catch (Exception e) {
            log.error("Tika 파일 파싱 실패: {}", file.getOriginalFilename(), e);
            throw new FileParseException("파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }

    // ===== Build DTO =====
    private ParseResultDto buildParseResult(String normalizedText) {
        Map<String, String> sections = splitSectionsByStructure(normalizedText);

        String gapguText = pickSection(sections, "갑구");
        String eulguText = pickSection(sections, "을구");

        boolean eulguEmpty = (eulguText == null || eulguText.isBlank() || eulguText.contains("기록사항 없음"));
        if (eulguEmpty) {
            eulguText = normalizedText;
        }

        return new ParseResultDto(
                parseAddress(normalizedText),
                parseUniqueNumber(normalizedText),
                parseCover(normalizedText),
                parseGapguSection(gapguText),
                parseEulguSection(eulguText)
        );
    }

    private String pickSection(Map<String, String> sections, String baseKey) {
        if (sections == null || sections.isEmpty()) return "";
        if (sections.containsKey(baseKey)) return sections.get(baseKey);
        for (String k : sections.keySet()) {
            if (k.equals(baseKey) || k.startsWith(baseKey + "#")) return sections.get(k);
        }
        for (String k : sections.keySet()) {
            String komp = k.replaceAll("\\s+", "");
            if (komp.equals(baseKey) || komp.startsWith(baseKey)) return sections.get(k);
        }
        return "";
    }

    // ===== Section Split =====
    private Map<String, String> splitSectionsByStructure(String text) {
        Map<String, String> sectionMap = new LinkedHashMap<>();
        Matcher m = TITLE_P.matcher(text);

        int lastEnd = 0;
        String lastTitle = null;
        int dup = 0;

        while (m.find()) {
            if (lastTitle != null) {
                String content = text.substring(lastEnd, m.start()).trim();
                String key = lastTitle;
                if (sectionMap.containsKey(key)) key = lastTitle + "#" + (++dup);
                sectionMap.put(key, content);
            }
            lastTitle = m.group(1).replaceAll("\\s", "");
            lastEnd = m.end();
        }

        if (lastTitle != null) {
            String tail = text.substring(lastEnd);
            tail = tail.replaceAll("-+\\s*이\\s*하\\s*여\\s*백\\s*-+", "").trim(); // "-- 이 하 여 백 --"
            String key = lastTitle;
            if (sectionMap.containsKey(key)) key = lastTitle + "#" + (++dup);
            sectionMap.put(key, tail);
        }
        return sectionMap;
    }

    // ===== Top-level metadata =====
    private String parseAddress(String text) {
        Pattern p = Pattern.compile("\\[집합건물\\]\\s*([^\\[]+?)\\s*고유번호");
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : null;
    }

    private String parseUniqueNumber(String text) {
        Matcher m = Pattern.compile("고유번호\\s*(\\d{4}-\\d{4}-\\d{6})").matcher(text);
        return m.find() ? m.group(1) : null;
    }

    // ===== [GENERIC] Cover(표제부) =====
    private ParseResultDto.CoverSection parseCover(String text) {
        // --- Address from header (already generic) ---
        String headerAddress = parseAddress(text);

        // --- [GENERIC] Building Name ---
        // 우선: "… 제XXX동 (아파트|오피스텔|…)" 형태를 전체문서에서 탐색
        String buildingName = null;
        Pattern bTypeP = Pattern.compile("([가-힣A-Za-z0-9·\\-\\s]{2,60}?제\\s*\\d+동\\s*(?:" + BUILDING_TYPES + "))");
        Matcher b1 = bTypeP.matcher(text);
        if (b1.find()) {
            buildingName = b1.group(1).replaceAll("\\s+", " ").trim();
        } else {
            // 보조: 건물유형이 명시 안 된 경우 "… 제XXX동"만 추출
            Pattern bNoTypeP = Pattern.compile("([가-힣A-Za-z0-9·\\-\\s]{2,60}?제\\s*\\d+동)");
            Matcher b2 = bNoTypeP.matcher(text);
            if (b2.find()) buildingName = b2.group(1).replaceAll("\\s+", " ").trim();
        }

        // --- [GENERIC] Structure (…조 …지붕) ---
        String structure = null;
        Pattern structP = Pattern.compile(
                "([가-힣A-Za-z\\s]*?(?:철근|철골|벽돌|목)?\\s*콘?크리트?\\s*조\\s*[가-힣A-Za-z\\s]*?지붕|[가-힣A-Za-z\\s]*?조\\s*[가-힣A-Za-z\\s]*?지붕)"
        );
        Matcher structM = structP.matcher(text);
        if (structM.find()) {
            structure = structM.group(1).replaceAll("\\s+", " ").trim();
        }

        // --- Total floors: 최대 층수 ---
        Integer totalFloors = null;
        Matcher floors = Pattern.compile("(\\d+)\\s*층").matcher(text);
        int max = 0;
        while (floors.find()) {
            try { max = Math.max(max, Integer.parseInt(floors.group(1))); } catch (Exception ignore) {}
        }
        totalFloors = max > 0 ? max : null;

        ParseResultDto.BuildingInfo main = new ParseResultDto.BuildingInfo(
                headerAddress,
                buildingName,
                structure,
                totalFloors == null ? 0 : totalFloors
        );

        // --- [GENERIC] Land (대지) ---
        List<ParseResultDto.LandInfo> lands = new ArrayList<>();
        // 풀패턴: "주소  지목  면적㎡"
        Pattern landFullP = Pattern.compile(
                "((?:[가-힣]+(?:특별시|광역시|도)\\s*)?" +          // 도/광역시 (선택)
                        "(?:[가-힣]+(?:시|군|구)\\s*)?" +                   // 시/군/구 (선택)
                        "(?:[가-힣0-9]+(?:읍|면|동)\\s*)?" +                // 읍/면/동 (선택)
                        "(?:[가-힣0-9\\-]+(?:리|가)?\\s*)?" +               // 리/가 (선택)
                        "\\d+(?:-\\d+)?\\s*)" +                            // 지번
                        "(" + LAND_CATEGORIES + ")\\s*" +
                        "([\\d,]+(?:\\.\\d+)?)㎡"
        );
        Matcher landM = landFullP.matcher(text);
        int seq = 1;
        while (landM.find()) {
            String addr = landM.group(1).replaceAll("\\s+", " ").trim();
            String category = landM.group(2).trim();
            double area = Double.parseDouble(landM.group(3).replace(",", ""));
            lands.add(new ParseResultDto.LandInfo(seq++, addr, category, area));
        }
        // 보조: "지목 면적㎡"만 있는 경우
        if (lands.isEmpty()) {
            Matcher onlyArea = Pattern.compile("(" + LAND_CATEGORIES + ")\\s*([\\d,]+(?:\\.\\d+)?)㎡").matcher(text);
            if (onlyArea.find()) {
                double area = Double.parseDouble(onlyArea.group(2).replace(",", ""));
                String baseAddr = Optional.ofNullable(headerAddress).orElse("");
                // 유닛 표기 제거: "… 제X층 제Y호 …"
                baseAddr = baseAddr.replaceAll("\\s*제\\s*\\d+층\\s*제\\s*\\d+호.*$", "").trim();
                lands.add(new ParseResultDto.LandInfo(1, baseAddr, onlyArea.group(1), area));
            }
        }

        // --- [GENERIC] Property part (전유부분) ---
        ParseResultDto.PropertyPartInfo part = null;
        // "제X층 제Y호 … NN.NN~NNN.NNN㎡"
        Matcher partM = Pattern.compile("(제\\s*\\d+층\\s*제\\s*\\d+호)[\\s\\S]*?(\\d{2,3}\\.\\d{2,3})㎡").matcher(text);
        if (partM.find()) {
            String floorUnit = partM.group(1).replaceAll("\\s+", " ").trim();
            double area = Double.parseDouble(partM.group(2));
            String partStruct = (structure != null) ? structure.replaceAll("\\s*지붕$", "") : "철근콘크리트조";
            part = new ParseResultDto.PropertyPartInfo(floorUnit, partStruct, area);
        }

        // --- [GENERIC] Land right (소유권대지권 A분의 B) ---
        List<ParseResultDto.LandRight> rights = new ArrayList<>();
        Matcher lr = Pattern.compile(
                K_RIGHT_LABEL + "[\\s\\S]{0,200}?(\\d+(?:\\.\\d+)?)\\s*분의\\s*(\\d+(?:\\.\\d+)?)"
        ).matcher(text);
        if (lr.find()) {
            rights.add(new ParseResultDto.LandRight("소유권대지권", lr.group(1) + "분의 " + lr.group(2)));
        }

        return new ParseResultDto.CoverSection(main, lands, part, rights);
    }

    // ===== 갑구 (헤더→청크→필드) =====
    private ParseResultDto.GapguSection parseGapguSection(String gapguText) {
        if (gapguText == null || gapguText.isEmpty() || gapguText.contains("기록사항 없음")) {
            return new ParseResultDto.GapguSection(new ArrayList<>());
        }

        List<ParseResultDto.OwnershipHistory> list = new ArrayList<>();
        List<int[]> ranges = locateEntryRanges(gapguText, ENTRY_HEAD_GAPGU);

        for (int[] rg : ranges) {
            String chunk = gapguText.substring(rg[0], rg[1]);

            Matcher h = ENTRY_HEAD_GAPGU.matcher(chunk);
            int seq = h.find() ? Integer.parseInt(h.group(1)) : -1;

            // 접수일 = 첫 번째 날짜
            LocalDate receipt = null;
            Matcher d = DATE_KR_P.matcher(chunk);
            if (d.find()) receipt = parseKoreanDate(d.group(0));

            String owner = cleanOwnerTag(findFirstGroup(Pattern.compile(K_OWNER + "\\s*([가-힣()]+)"), chunk));
            String cause = findFirstGroup(Pattern.compile(K_JEHO + "\\s*([가-힣]+)"), chunk);

            String priceStr = findFirstGroup(Pattern.compile("거\\s*래\\s*가\\s*액\\s*금\\s*([\\d,]+)원"), chunk);
            Long price = priceStr != null ? Long.parseLong(priceStr.replace(",", "")) : null;

            if (seq > 0 && receipt != null && owner != null && cause != null) {
                list.add(new ParseResultDto.OwnershipHistory(seq, receipt, cause, owner, price));
            } else {
                log.warn("갑구 청크 파싱 미완성: seq={}, receipt={}, owner={}, cause={}, chunk='{}'",
                        seq, receipt, owner, cause, abbreviate(chunk, 180));
            }
        }

        list.sort(Comparator.comparingInt(ParseResultDto.OwnershipHistory::sequence));
        return new ParseResultDto.GapguSection(list);
    }

    // ===== 을구: 라인 스캔(상태머신) + 정밀 로깅 =====
    private ParseResultDto.EulguSection parseEulguSection(String eulguText) {
        List<ParseResultDto.LienHistory> list = new ArrayList<>();

        if (eulguText == null || eulguText.isBlank()) {
            return new ParseResultDto.EulguSection(list);
        }
        if (eulguText.contains("기록사항 없음")) {
            log.info("EULGU DIAG: contains '기록사항 없음' → will still scan lines for safety");
        }

        final Pattern HEAD         = Pattern.compile("^\\s*(\\d+)\\s*" + K_LIEN + "\\s*");
        final Pattern MAXAMT       = Pattern.compile(K_MAXAMT + "\\s*금\\s*([\\d,]+)원(?:정)?");
        final Pattern JEHO_CAUSE   = Pattern.compile(K_JEHO + "\\s*([가-힣]+)");
        final Pattern DEBTOR       = Pattern.compile(K_DEBTOR + "\\s*([가-힣]+)");
        final Pattern CREDITOR_ONE = Pattern.compile(K_LIEN_HOLDER + "\\s*(.+)$");

        String[] lines = eulguText.split("\\R", -1);
        Integer curSeq = null;
        LocalDate curReceipt = null;
        Long curAmount = null;
        String curCause = null;
        String curDebtor = null;
        String curCreditor = null;

        for (int i = 0; i < lines.length; i++) {
            String raw = lines[i];
            String line = raw.trim();

            if (line.isEmpty()
                    || line.startsWith("[집합건물]")
                    || line.matches("^\\d+/\\d+$")
                    || line.contains("열람일시")
                    || line.contains("관할등기소")
                    || line.contains("본 등기사항증명서는")
                    || line.contains("기록사항 없는")) {
                continue;
            }

            String lineMarked   = markInvisibles(line);
            String noSpaceToken = line.replaceAll("[\\uFEFF\\u200C\\u200D\\u2060\\u00A0\\s]", "");

            Matcher mh = HEAD.matcher(line);
            if (mh.find()) {
                if (curSeq != null) {
                    if (curReceipt != null && curAmount != null && curCause != null && curDebtor != null) {
                        list.add(new ParseResultDto.LienHistory(curSeq, "근저당권설정", curReceipt, curCause,
                                curDebtor, curCreditor == null ? "" : curCreditor, curAmount, false));
                    } else {
                        List<String> miss = new ArrayList<>();
                        if (curReceipt == null) miss.add("receiptDate");
                        if (curAmount  == null) miss.add("amount");
                        if (curCause   == null) miss.add("cause");
                        if (curDebtor  == null) miss.add("debtor");
                    }
                }

                curSeq = Integer.parseInt(mh.group(1));
                curReceipt = null; curAmount = null; curCause = null; curDebtor = null; curCreditor = null;

                Matcher dInline = DATE_KR_P.matcher(line);
                if (dInline.find()) {
                    curReceipt = parseKoreanDate(dInline.group(0));
                }
                Matcher maInline = MAXAMT.matcher(line);
                if (maInline.find()) {
                    curAmount = Long.parseLong(maInline.group(1).replace(",", ""));
                }
                Matcher jcInline = JEHO_CAUSE.matcher(line);
                if (jcInline.find()) {
                    curCause = jcInline.group(1);
                }
                Matcher dbInline = DEBTOR.matcher(line);
                if (dbInline.find()) {
                    curDebtor = dbInline.group(1);
                }
                Matcher crInline = CREDITOR_ONE.matcher(line);
                if (crInline.find()) {
                    String rawCred = crInline.group(1).trim();
                    String cred = rawCred.replaceAll("\\s*\\d[\\d-]*.*$", "").trim().replaceAll("\\s+", " ");
                    curCreditor = cred;
                }
                continue;
            }

            if (curSeq != null) {
                if (curReceipt == null) {
                    Matcher d = DATE_KR_P.matcher(line);
                    if (d.find()) {
                        curReceipt = parseKoreanDate(d.group(0));
                    }
                }
                if (curAmount == null) {
                    Matcher ma = MAXAMT.matcher(line);
                    if (ma.find()) {
                        curAmount = Long.parseLong(ma.group(1).replace(",", ""));
                    }
                }
                if (curCause == null) {
                    Matcher jc = JEHO_CAUSE.matcher(line);
                    if (jc.find()) {
                        curCause = jc.group(1);
                    }
                }
                if (curDebtor == null) {
                    Matcher db = DEBTOR.matcher(line);
                    if (db.find()) {
                        curDebtor = db.group(1);
                    }
                }
                if (curCreditor == null) {
                    Matcher cr = CREDITOR_ONE.matcher(line);
                    if (cr.find()) {
                        String rawCred = cr.group(1).trim();
                        String cred = rawCred.replaceAll("\\s*\\d[\\d-]*.*$", "").trim().replaceAll("\\s+", " ");
                        curCreditor = cred;
                    }
                }
            } else {
                if (line.contains("근저당")) {
                    log.warn("EULGU WARN: found '근저당' but no active entry at line {}: '{}'", i, safeAbbrev(lineMarked, 160));
                }
            }
        }

        if (curSeq != null) {
            if (curReceipt != null && curAmount != null && curCause != null && curDebtor != null) {
                list.add(new ParseResultDto.LienHistory(curSeq, "근저당권설정", curReceipt, curCause,
                        curDebtor, curCreditor == null ? "" : curCreditor, curAmount, false));
            } else {
                List<String> miss = new ArrayList<>();
                if (curReceipt == null) miss.add("receiptDate");
                if (curAmount  == null) miss.add("amount");
                if (curCause   == null) miss.add("cause");
                if (curDebtor  == null) miss.add("debtor");
            }
        }

        applyTerminationsRobustFuzzy(eulguText, list);

        list.sort(Comparator.comparingInt(ParseResultDto.LienHistory::sequence));
        return new ParseResultDto.EulguSection(list);
    }

    // ===== 공통: 엔트리 범위 산출 (갑구에서 사용) =====
    private List<int[]> locateEntryRanges(String text, Pattern headPattern) {
        List<int[]> ranges = new ArrayList<>();
        Matcher m = headPattern.matcher(text);
        List<Integer> starts = new ArrayList<>();
        while (m.find()) starts.add(m.start());
        for (int i = 0; i < starts.size(); i++) {
            int s = starts.get(i);
            int e = (i + 1 < starts.size()) ? starts.get(i + 1) : text.length();
            ranges.add(new int[]{s, e});
        }
        return ranges;
    }

    // ===== 말소/해지 (fuzzy) =====
    private void applyTerminationsRobustFuzzy(String eulguText, List<ParseResultDto.LienHistory> list) {
        String TERM = "(?:" + K_TERMINATE1 + "|" + K_TERMINATE2 + ")";

        Pattern termLine = Pattern.compile("(?s)((?:\\d+\\s*번\\s*" + K_LIEN + "(?:,\\s*)?\\s*)+)[\\s\\S]*?" + TERM);
        Pattern termSingle = Pattern.compile("(?s)(\\d+)\\s*번\\s*" + K_LIEN + "(?:등)?[\\s\\S]*?" + TERM);

        Set<Integer> terminated = new HashSet<>();

        Matcher t1 = termLine.matcher(eulguText);
        while (t1.find()) {
            Matcher pick = Pattern.compile("(\\d+)\\s*번\\s*" + K_LIEN).matcher(t1.group(1));
            while (pick.find()) terminated.add(Integer.parseInt(pick.group(1)));
        }

        Matcher t2 = termSingle.matcher(eulguText);
        while (t2.find()) terminated.add(Integer.parseInt(t2.group(1)));

        String[] lines = eulguText.split("\\R", -1);
        Pattern numP  = Pattern.compile("(\\d+)\\s*번\\s*" + K_LIEN + "(?:등)?");
        Pattern termP = Pattern.compile(K_TERMINATE1 + "|" + K_TERMINATE2);
        final int WINDOW = 2;
        Deque<Integer> buffer = new ArrayDeque<>();
        int lastNumLine = -9999;

        for (int i = 0; i < lines.length; i++) {
            String ln = lines[i];

            Matcher n = numP.matcher(ln);
            boolean foundNum = false;
            while (n.find()) {
                buffer.add(Integer.parseInt(n.group(1)));
                foundNum = true;
            }
            if (foundNum) lastNumLine = i;

            Matcher tt = termP.matcher(ln);
            if (tt.find() && !buffer.isEmpty() && (i - lastNumLine) <= WINDOW) {
                terminated.addAll(buffer);
                buffer.clear();
            }

            if (!buffer.isEmpty() && (i - lastNumLine) > WINDOW) buffer.clear();
        }

        for (int i = 0; i < list.size(); i++) {
            ParseResultDto.LienHistory h = list.get(i);
            if (terminated.contains(h.sequence())) {
                list.set(i, new ParseResultDto.LienHistory(
                        h.sequence(), h.type(), h.receiptDate(), h.cause(),
                        h.debtor(), h.creditor(), h.amount(), true
                ));
            }
        }
    }
}
