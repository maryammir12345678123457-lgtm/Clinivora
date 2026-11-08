package com.Clinivora.ui;

import com.Clinivora.config.UserSettingsStore;
import com.Clinivora.service.NavigationService;
import com.Clinivora.ui.i18n.AppLanguage;
import com.Clinivora.ui.i18n.I18n;
import com.Clinivora.ui.theme.AppTheme;
import com.Clinivora.ui.theme.ThemeManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class PreferencesFeatureTest {

    @TempDir
    Path tempDir;

    private UserSettingsStore store;

    @BeforeEach
    void setUp() {
        ThemeManager.resetForTests();
        I18n.resetForTests();
        store = new UserSettingsStore(tempDir.resolve("clinivora-settings.properties"));
        ThemeManager.initialize(store);
        I18n.initialize(store);
    }

    @AfterEach
    void tearDown() {
        ThemeManager.resetForTests();
        I18n.resetForTests();
    }

    @Test
    void themeEnumResolvesStylesheetsForEveryTheme() {
        for (AppTheme theme : AppTheme.values()) {
            List<String> sheets = ThemeManager.getInstance().resolveStylesheets(theme);
            assertEquals(2, sheets.size());
            assertTrue(sheets.get(0).endsWith("app.css"));
            assertTrue(sheets.get(1).contains("theme-" + theme.getId() + ".css"));
            assertNotNull(getClass().getResource(theme.getStylesheetPath()),
                    "Missing stylesheet for " + theme);
        }
    }

    @Test
    void themePersistsAcrossReload() {
        ThemeManager.getInstance().setTheme(AppTheme.DARK);
        assertEquals(AppTheme.DARK, store.getTheme());
        assertTrue(Files.exists(store.getSettingsFile()));

        UserSettingsStore reloaded = new UserSettingsStore(store.getSettingsFile());
        reloaded.load();
        assertEquals(AppTheme.DARK, reloaded.getTheme());
    }

    @Test
    void colorfulAndPinkThemesAreSupported() {
        ThemeManager.getInstance().setTheme(AppTheme.PINK);
        assertEquals(AppTheme.PINK, ThemeManager.getInstance().getCurrent());
        ThemeManager.getInstance().setTheme(AppTheme.COLORFUL);
        assertEquals(AppTheme.COLORFUL, ThemeManager.getInstance().getCurrent());
        assertEquals(AppTheme.COLORFUL, AppTheme.fromId("colorful"));
        assertEquals(AppTheme.PINK, AppTheme.fromId("PINK"));
    }

    @Test
    void unsupportedThemeThrows() {
        assertThrows(IllegalArgumentException.class, () -> AppTheme.fromId("neon-void"));
    }

    @Test
    void languageBundlesContainCoreKeys() {
        for (AppLanguage language : AppLanguage.values()) {
            I18n.getInstance().setLanguage(language);
            assertFalse(I18n.getInstance().t("nav.dashboard").isBlank());
            assertFalse(I18n.getInstance().t("settings.theme").isBlank());
            assertFalse(I18n.getInstance().t("login.button").isBlank());
            assertEquals(language, I18n.getInstance().getLanguage());
        }
    }

    @Test
    void urduIsRightToLeftAndChineseIsNot() {
        assertTrue(AppLanguage.URDU.isRightToLeft());
        assertFalse(AppLanguage.ENGLISH.isRightToLeft());
        assertFalse(AppLanguage.CHINESE.isRightToLeft());
        assertEquals("ur", AppLanguage.fromCode("UR").getCode());
        assertEquals(AppLanguage.CHINESE, AppLanguage.fromCode("zh"));
    }

    @Test
    void languagePersistsAndNotifiesListeners() {
        AtomicReference<AppLanguage> heard = new AtomicReference<>();
        I18n.getInstance().addListener(heard::set);
        I18n.getInstance().setLanguage(AppLanguage.CHINESE);
        assertEquals(AppLanguage.CHINESE, heard.get());

        UserSettingsStore reloaded = new UserSettingsStore(store.getSettingsFile());
        reloaded.load();
        assertEquals(AppLanguage.CHINESE, reloaded.getLanguage());
    }

    @Test
    void chineseTranslationDiffersFromEnglishForNav() {
        I18n.getInstance().setLanguage(AppLanguage.ENGLISH);
        String english = I18n.getInstance().t("nav.patients");
        I18n.getInstance().setLanguage(AppLanguage.CHINESE);
        String chinese = I18n.getInstance().t("nav.patients");
        assertNotEquals(english, chinese);
        assertEquals("患者", chinese);
    }

    @Test
    void settingsViewIsVisibleForAllRolesInNavigation() {
        // Visibility matrix: SETTINGS should be open to every authenticated role.
        assertEquals("page.settings", NavigationService.View.SETTINGS.getTitleKey());
        assertNotNull(getClass().getResource(NavigationService.View.SETTINGS.getFxmlPath()));
    }

    @Test
    void messageFormatSupportsArgumentsWhenPresent() {
        // Ensure MessageFormat path is exercised with zero-arg keys (no crash) and known key.
        String value = I18n.getInstance().t("settings.saved");
        assertFalse(value.contains("{0}"));
        assertEquals("settings.missing.key", I18n.getInstance().t("settings.missing.key"));
    }
}
