package com.jkproject.JkProject.gemini;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiService geminiService;


    @GetMapping("/gemini/today/clock")
    public CompletableFuture<String> todayClock () {
        LocalDateTime current = LocalDateTime.now();

        String formatedNow = current.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm:ss"));

        String prompt = "# Role\n" +
                "너는 대형 증권사 리서치 센터의 '주도주 발굴 전문가'이자 '섹터 애널리스트'야. \n" +
                formatedNow + "의 국내 증시(KOSPI, KOSDAQ) 데이터를 실시간 검색하여, 오늘 가장 유의미한 흐름을 보이는 종목들을 선정하고 분석 리포트를 작성해.\n" +
                "\n" +
                "# Task\n" +
                "1. 시장에서 거래량이 폭발하며 주가가 급등한 종목들 중 '단순 테마'가 아닌 '실질적 모멘텀'을 가진 종목 20개를 선정해.\n" +
                "2. 선정된 종목들을 공통된 성격에 따라 '테마별'로 그룹화해. (예: AI 인프라, 전력 설비, K-원전 등)\n" +
                "3. 각 종목에 대해 아래 [출력 양식]을 엄격히 준수하여 분석해.\n" +
                "\n" +
                "# Output Format\n" +
                "## [테마명: 예시 - AI 반도체 및 고대역폭 메모리(HBM)]\n" +
                "- 테마 특징: 현재 이 테마가 시장을 주도하는 이유와 전반적인 수급 상황을 1줄로 요약.\n" +
                "- 가독성을 위해 모든 내용은 <div>, <table>, <span> 태그를 사용해.\n" +
                "- 인라인 CSS(style=\"\")를 활용해 유료 리포트 느낌의 고급스러운 디자인을 적용해.\n" +
                "- 배경색은 연한 회색(#f9f9f9) 또는 연한 파란색(#eff6ff) 박스를 사용해 섹션을 구분해.\n" +
                "- 강조할 종목명은 굵은 글씨와 파란색(#003366)을 사용해.\n" +
                "\n" +
                "종목명 (시가총액) +등락률%\n" +
                "- 상승 원인: 오늘 주가를 움직인 결정적인 뉴스, 공시, 또는 수급 촉매제(Catalyst) 기술.\n" +
                "- 향후 전망: 기술적 위치(신고가 등)와 향후 예상되는 추가 모멘텀 및 대응 전략.\n" +
                "\n" +
                "# Rules\n" +
                "- 인사말과 서론은 생략하고 즉시 본론(테마별 분석)부터 시작할 것.\n" +
                "- 시가총액과 등락률은 실시간 검색 데이터를 바탕으로 정확히 기재할 것. [cite: 94, 108]\n" +
                "- 전문적인 금융 용어(Relative Strength, Sector Rotation, CAPEX 등)를 사용하여 전문성을 높일 것.\n" +
                "- 마지막에는 반드시 표준 [면책 조항]을 포함할 것. [cite: 203, 269]";

        return geminiService.generateMarketCommentary(prompt, "주도주 발굴");
    }
    @GetMapping("/gemini/today/news")
    public CompletableFuture<String> todayNews () {
        String prompt = "# Role\n" +
                "너는 15년 경력의 시니어 투자전략 애널리스트이자 퀀트 분석가야. \n" +
                "어제 장 마감 이후부터 현재 시점까지 발생한 방대한 금융 데이터를 필터링하여, \n" +
                "투자자가 개장 즉시 수익을 낼 수 있는 '개장 전 핵심 전략 리포트'를 작성하는 것이 네 임무야.\n" +
                "\n" +
                "# Objective\n" +
                "제공된 시장 데이터(국내 공시, 미 증시 지표, 외신 뉴스 등)를 분석하여 아래 3개 섹션을 HTML 형식으로 출력해. \n" +
                "서론과 인사말은 완전히 생략하고 바로 본론으로 들어갈 것.\n" +
                "\n" +
                "# Output Format (HTML/CSS)\n" +
                "- 가독성을 위해 모든 내용은 <div>, <table>, <span> 태그를 사용해.\n" +
                "- 인라인 CSS(style=\"\")를 활용해 유료 리포트 느낌의 고급스러운 디자인을 적용해.\n" +
                "- 배경색은 연한 회색(#f9f9f9) 또는 연한 파란색(#eff6ff) 박스를 사용해 섹션을 구분해.\n" +
                "- 강조할 종목명은 굵은 글씨와 파란색(#003366)을 사용해.\n" +
                "\n" +
                "# Content Requirements\n" +
                "1. 국내 시장 사후/사전 브리핑:\n" +
                "   - 장 마감 후 주요 공시(공급계약, 증자, 지분변동 등) 분석.\n" +
                "   - 시간외 단일가 특징주와 거래대금 상위 종목 분석.\n" +
                "   - 당일 발표될 정부 정책이나 경제 지표가 시초가에 미칠 영향 기술.\n" +
                "\n" +
                "2. 미 증시 하이라이트:\n" +
                "   - 뉴욕 3대 지수 등락의 핵심 트리거 분석 (매크로 지표 또는 Fed 발언).\n" +
                "   - 실적 발표 빅테크 기업의 가이던스 변화가 한국 반도체/2차전지에 미칠 영향.\n" +
                "   - 미 증시 내 업종별 등락(Sector Heatmap) 분석 및 강세 섹터 명시.\n" +
                "\n" +
                "3. 개장 직후 공략 테마 및 종목:\n" +
                "   - 위 데이터들을 종합해 오늘 아침 수급 쏠림이 100% 예상되는 핵심 테마 2~3개 선정.\n" +
                "   - 테마별 대장주 1개, 부대장주 1개 선정.\n" +
                "   - [선정이유]와 [기대 모멘텀(목표 타겟가 등)]을 구체적 수치와 근거를 들어 설명.\n" +
                "\n" +
                "# Constraint\n" +
                "- 전문 용어(CAPEX, HBM, FOMC 매파적 발언 등)를 적절히 섞어 전문성을 높여.\n" +
                "- 마지막엔 반드시 표준 [면책 조항]을 포함해.";

        return geminiService.generateMarketCommentary(prompt, "장 전 뉴스");
    }

    @GetMapping("/gemini/today/status")
    public CompletableFuture<String> todayStatus () {
        String prompt = "";

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formatedNow = now.format(formatter);
        String timePrompt = "";
        if (LocalTime.now().isAfter(LocalTime.of(13, 00))) {
            timePrompt = formatedNow + " 국내 주식시장의 **오전장(09:00~12:00)과 오후장(12:00~종가)";
        } else {
            timePrompt = formatedNow + " 국내 주식시장의 오전장(09:00~12:00)";
        }

        prompt = "# Role\n" +
                "너는 대형 증권사 리서치 센터의 '시황 전략 팀장'이자 '수석 애널리스트'야. \n" +
                "현재 시각(" + formatedNow + ")의 대한민국 주식시장 데이터를 실시간으로 검색하여 \n" +
//                "현재 시각(2026년 5월 7일 오전)의 대한민국 주식시장 데이터를 실시간으로 검색하여 \n" +
                "기관 투자자들에게 배포할 수준의 전문적인 '오전장 전략 브리핑'을 작성해.\n" +
                "\n" +
                "# Objective\n" +
//                "오늘 오전(09:00~12:00)의 시장 흐름을 3개 섹션으로 요약하여 HTML 형식으로 출력해. \n" +
                timePrompt + "의 시장 흐름을 3개 섹션으로 요약하여 HTML 형식으로 출력해. \n" +
                "인사말과 서론은 생략하고 즉시 리포트 본론으로 들어갈 것.\n" +
                "\n" +
                "# Style & Formatting\n" +
                "- 모든 응답은 <div>, <table>, <span> 태그를 사용하여 구조화해.\n" +
                "- 전문적인 유료 리포트 느낌의 인라인 CSS(style)를 적용해.\n" +
                "- 강조할 섹션 제목은 배경색(#f1f5f9)과 진한 네이비색(#0f172a)을 사용해.\n" +
                "- 금융 전문 용어(Relative Strength, Sector Rotation, Liquidity Risk, Alpha, Beta 등)를 적극 사용해.\n" +
                "\n" +
                "# Content Requirements\n" +
                "1. Market Sentiment (수급 및 섹터 분석):\n" +
                "   - 코스피/코스닥 지수 흐름 및 외인/기관의 순매수 대금 분석.\n" +
                "   - 섹터별 강세/약세 테마 분류 (예: 반도체 주도권, 2차전지 반등 여부 등).\n" +
                "   - 수급 쏠림이 발생하는 특정 테마의 변동 원인(Catalyst) 기술.\n" +
                "\n" +
                "2. Key Drivers (결정적 모멘텀):\n" +
                "   - 오늘 오전 장세에 결정적 영향을 미친 매크로 지표(환율, 국채금리 등) 분석.\n" +
                "   - 장중 발표된 주요 뉴스, 기업 공시, 혹은 정부 정책 브리핑.\n" +
                "   - 글로벌 증시(미 야간 선물 등)와의 동조화 현상 분석.\n" +
                "\n" +
                "3. Analyst View (전략 및 유의사항):\n" +
                "   - 현재 장세를 관통하는 핵심 키워드 3가지 추출.\n" +
                "   - 오후장 변동성에 대비한 투자자 유의사항 및 리스크 관리 전략.\n" +
                "   - 단기 트레이딩 관점에서의 비중 조절 조언.\n" +
                "\n" +
                "# Constraint\n" +
                "- '현상'만 나열하지 말고, 그 이면의 '원인'과 '영향'을 분석적 시각으로 작성해.\n" +
                "- 마지막에 표준 [면책 조항]을 포함할 것.";

        return geminiService.generateMarketCommentary(prompt, "현재 시황");
    }
}
