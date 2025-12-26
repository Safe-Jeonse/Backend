# 안심 전세 (Safe-Jeonse)
본 프로젝트는 전세 사기 피해 예방을 목적으로, 국토교통부 공공데이터와 생성형 AI를 결합하여 임대차 계약의 위험도를 자동으로 분석하는 웹 서비스입니다. 
2030 청년층 등 부동산 거래 경험이 적은 사용자들이 복잡한 등기부등본과 건축물대장 데이터를 직관적으로 이해할 수 있도록 돕습니다.

## 1. 프로젝트 개요
수행 기간: 2025.09.01. ~ 2025.12.22.

참여 인원: 조중현(프론트엔드), 김선호(백엔드, PM)

주요 목표: 생성형 AI(Gemini, Groq)를 활용한 등기부등본 분석 및 맞춤형 전세 위험도 리포트 생성

## 2. 주요 기능
AI 기반 위험도 분석 (Quick & Deep Check)
Quick Check: 등기부등본 없이 주소와 보증금 정보만으로 공공데이터 기반 기초 위험도를 즉시 분석합니다.

Deep Check: 사용자가 업로드한 등기부등본(PDF)을 Apache Tika로 파싱하여 선순위 채권, 소유권 변동 이력, 신탁 여부 등을 정밀 분석합니다.

설명 가능한 상세 리포트
안전 등급 산출: 분석 결과를 안전, 주의, 위험, 고위험 4단계로 분류하여 시각화합니다.

AI 종합 의견: 생성형 AI가 해당 매물의 위험 사유와 판단 근거를 자연어로 요약하여 제공합니다.

시세 교차 검증: Tavily Search API를 통해 최신 실거래가 정보를 수집하고 AI가 적정 시세를 추정하여 전세가율을 계산합니다.

사용자 지원 도구
휴먼 체크리스트: 계약 당일 임대인 신분 확인, 특약 사항 삽입 등 실무적인 체크리스트를 제공합니다.

예방 가이드: 깡통전세, 이중계약 등 주요 사기 유형별 대응 방안과 교육 정보를 제공합니다.

## 3. 기술 스택
Backend
Language: Java 21

Framework: Spring Boot 3.5.6

Database: MariaDB 10.11, Spring Data JPA

Security: Spring Security

Parsing: Apache Tika 3.2.2 (PDF 분석)

## AI & API
AI Models: Groq (Llama 3), Google Gemini 2.0 Flash Lite

Search: Tavily Search API (웹 데이터 교차 검증)

Public Data: 국토교통부 공공데이터 API, SGIS(통계지리정보서비스), VWorld 주소 API

Infrastructure
Container: Docker, Docker Compose

Deployment: Vercel (Frontend), Docker-based Backend

## 4. 시스템 아키텍처 및 데이터 흐름
데이터 수집: 주소 입력 시 SGIS 및 VWorld API를 통해 PNU 코드 및 건물 정보를 획득합니다.

문서 파싱: 사용자가 업로드한 등본 PDF에서 갑구(소유권), 을구(근저당) 정보를 정형 데이터로 추출합니다.

데이터 통합: 공공데이터(건축물대장, 실거래가)와 파싱된 데이터를 통합하여 AI 프롬프트를 구성합니다.

AI 추론: PromptManager를 통해 최적화된 프롬프트를 AI 모델에 전달하고 분석 리포트를 생성합니다.

## 5. 실행 방법
환경 변수 설정
프로젝트 루트 디렉토리에 .env 파일을 생성하고 아래 항목을 설정해야 합니다.

코드 스니펫

```
# Database
MYSQL_ROOT_PASSWORD=your_password
MYSQL_DATABASE=jeonse_db
MYSQL_USER=your_user
MYSQL_PASSWORD=your_password

# AI API Keys
OPENAI_API_KEY=your_groq_api_key
GEMINI_KEY=your_gemini_key
TAVILY_API_KEY=your_tavily_key

# Public Data API Keys
ADDRESS_API_KEY=your_vworld_key
SGIS_KEY=your_sgis_key
SGIS_SECRET=your_sgis_secret
OPEN_DATA_KEY=your_molit_api_key
```

도커를 이용한 실행
```
Bash

# 1. 소스 코드 빌드
./gradlew build

# 2. 컨테이너 실행
docker-compose up -d
```
