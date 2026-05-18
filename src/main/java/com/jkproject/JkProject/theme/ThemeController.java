package com.jkproject.JkProject.theme;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/theme")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    @GetMapping("/sync")
    public ResponseEntity<String> syncThemes() {
        themeService.syncThemesFromNaver();
        return ResponseEntity.ok("성공적으로 모든 테마와 종목 매핑을 DB에 저장했습니다!");
    }
}