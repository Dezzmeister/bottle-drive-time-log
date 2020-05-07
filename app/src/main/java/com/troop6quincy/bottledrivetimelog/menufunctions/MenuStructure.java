package com.troop6quincy.bottledrivetimelog.menufunctions;

import com.troop6quincy.bottledrivetimelog.R;

/**
 * Lists item IDs for items in the main action menu. This is done so that the text color can be
 * programatically changed when the theme is changed.
 */
public class MenuStructure {

    /**
     * {@link com.troop6quincy.bottledrivetimelog.MainActivity MainActivity} Menu Items
     */
    public static final int[] MENU_IDS = {
            R.id.main_menu_export_excel, R.id.main_menu_export_excel_old,
            R.id.main_menu_export_text, R.id.main_menu_set_money_earned,
            R.id.main_menu_clear_entries, R.id.main_menu_toggle_theme
    };
}
