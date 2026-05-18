package com.jkproject.JkProject;

import com.jkproject.JkProject.stockHistory.StockHistoryConfig;
import com.jkproject.JkProject.stockHistory.StockHistoryService;
import com.jkproject.JkProject.stockHistory.dto.StockHotResDto;
import com.jkproject.JkProject.stockTheme.dto.StockThemeResDto;
import com.jkproject.JkProject.util.TelegramService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest

class JkProjectApplicationTests {
	@Autowired
	private StockHistoryService stockHistoryService;

	@Autowired
	private TelegramService telegramService;

	@Value("${gemini.api.key}")
	private static String geminiKey;
	@Value("${kiwoom.app.key}")
	private static String appKey;
	@Value("${kiwoom.secret.key}")
	private static String secretKey;
	@Test
	void propertiesTest(){
		System.out.println("geminiKey : " + geminiKey);
		System.out.println("appKey : " + appKey);
		System.out.println("secretKey" + secretKey);
	}

	@Test
	void contextLoads() {
		System.out.println("test gogo");
	}

	@Test
	void telegramTest(){
		telegramService.printToken();
	}

	@Test
	void sendPdfTest() {
		String filePath = "reports/외인 및 기관 3일 매수_0518_1608.pdf";


		telegramService.sendPdf("chatK", filePath);
		telegramService.sendPdf("chatS", filePath);
	}

	@Test
	void hotTest() {
		String time = "2026-04-29 09:27:33.000";
		String formatTime = String.format("%s:%s:%s",time.substring(0, 2), time.substring(2, 4), time.substring(4, 6));
		List<StockThemeResDto> themeDto = new ArrayList<StockThemeResDto>();
		themeDto.add(new StockThemeResDto("90",	"125490",	"자동차부품"));
		themeDto.add(new StockThemeResDto("154",	"125490",	"2025 하반기 신규상장"));
		themeDto.add(new StockThemeResDto("190",	"125490",	"로봇(산업용/협동로봇 등)"));
		themeDto.add(new StockThemeResDto("194",	"125490",	"지능형로봇/인공지능(AI)"));
		themeDto.add(new StockThemeResDto("213",	"125490",	"자율주행차"));
		themeDto.add(new StockThemeResDto("237",	"125490",	"피지컬 AI/휴머노이드 로봇"));


		StockHotResDto hotStock = StockHotResDto.builder()
				.stockCode("125490")
				.stockName("한라캐스트")
				.type("자동차 신품 부품 제조업")
				.searchType("17") // DB에 저장된 업종명
				.date(formatTime)
				.diffRate(0.0) // 💡 대장주를 가리기 위해 이 값이 꼭 필요합니다!
				.stockThemes(themeDto)
				.build();
//
		StockHistoryConfig.detectedStocksMap.put("125490", hotStock);

		themeDto = new ArrayList<StockThemeResDto>();
		themeDto.add(new StockThemeResDto("37",	"011070",	"무선충전기술"));
		themeDto.add(new StockThemeResDto("79",	"011070",	"아이폰"));
		themeDto.add(new StockThemeResDto("90",	"011070",	"자동차부품"));
		themeDto.add(new StockThemeResDto("108",	"011070",	"스마트폰"));
		themeDto.add(new StockThemeResDto("144",	"011070",	"코리아 밸류업 지수(Korea Value-up Index)"));
		themeDto.add(new StockThemeResDto("162",	"011070",	"밸류업(24년 기업가치 제고계획 발표)"));
		themeDto.add(new StockThemeResDto("169",	"011070",	"메타버스(Metaverse)"));
		themeDto.add(new StockThemeResDto("211",	"011070",	"카메라모듈/부품"));
		themeDto.add(new StockThemeResDto("213",	"011070",	"자율주행차"));
		themeDto.add(new StockThemeResDto("235",	"011070",	"스마트카(SMART CAR)"));
		themeDto.add(new StockThemeResDto("237",	"011070",	"피지컬 AI/휴머노이드 로봇"));


		hotStock = StockHotResDto.builder()
				.stockCode("011070")
				.stockName("LG이노텍")
				.type("전자부품 제조업")
				.searchType("17") // DB에 저장된 업종명
				.date(formatTime)
				.diffRate(0.0) // 💡 대장주를 가리기 위해 이 값이 꼭 필요합니다!
				.stockThemes(themeDto)
				.build();
//
		StockHistoryConfig.detectedStocksMap.put("011070", hotStock);

		if (StockHistoryConfig.detectedStocksMap.size() >= 2) {
			String alertMsg = stockHistoryService.analyzeMarketLeaders(new ArrayList<>(StockHistoryConfig.detectedStocksMap.values()));
			System.out.println(alertMsg);
//
//                                // 분석 결과가 잘 나왔다면 프론트엔드 알림 채널로 쏴줍니다.
//                                if (!alertMsg.isEmpty()) {
//                                    messagingTemplate.convertAndSend("/topic/alerts", alertMsg);
//                                }
		}
	}
}
