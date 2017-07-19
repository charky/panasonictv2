/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.panasonictv2.internal.protocol;

/**
 * The {@link KeyCode} presents all available key codes of Panasonic TV.
 *
 * @author Charky - Initial contribution
 */
public enum KeyCode {

    NRC_POWER("NRC_POWER-ONOFF"), // power off only (on tested TV)

    NRC_MUTE("NRC_MUTE-ONOFF"),
    NRC_AD_CHANGE("NRC_AD_CHANGE-ONOFF"), // dvbt input change
    NRC_CHG_INPUT("NRC_CHG_INPUT-ONOFF"), // hdmi input change

    NRC_VOLDOWN("NRC_VOLDOWN-ONOFF"), // separate ON and OFF commands exists, can be used instead repeating ONOFF
    NRC_VOLDOWN_ON("NRC_VOLDOWN-ON"),
    NRC_VOLDOWN_OFF("NRC_VOLDOWN-OFF"),
    NRC_VOLUP("NRC_VOLUP-ONOFF"), // separate ON and OFF commands exists, can be used instead repeating ONOFF
    NRC_VOLUP_ON("NRC_VOLUP-ON"),
    NRC_VOLUP_OFF("NRC_VOLUP-OFF"),

    NRC_CH_DOWN("NRC_CH_DOWN-ONOFF"),
    NRC_CH_UP("NRC_CH_UP-ONOFF"),

    NRC_APPS("NRC_APPS-ONOFF"),
    NRC_HOME("NRC_HOME-ONOFF"),
    NRC_CANCEL("NRC_CANCEL-ONOFF"), // exit button

    NRC_RIGHT("NRC_RIGHT-ONOFF"), // separate ON and OFF commands exists, can be used instead repeating ONOFF
    NRC_RIGHT_ON("NRC_RIGHT-ON"),
    NRC_RIGHT_OFF("NRC_RIGHT-OFF"),

    NRC_LEFT("NRC_LEFT-ONOFF"), // separate ON and OFF commands exists, can be used instead repeating ONOFF
    NRC_LEFT_ON("NRC_LEFT-ON"),
    NRC_LEFT_OFF("NRC_LEFT-OFF"),

    NRC_DOWN("NRC_DOWN-ONOFF"), // separate ON and OFF commands exists, can be used instead repeating ONOFF
    NRC_DOWN_ON("NRC_DOWN-ON"),
    NRC_DOWN_OFF("NRC_DOWN-OFF"),

    NRC_UP("NRC_UP-ONOFF"), // separate ON and OFF commands exists, can be used instead repeating ONOFF
    NRC_UP_ON("NRC_UP-ON"),
    NRC_UP_OFF("NRC_UP-OFF"),

    NRC_ENTER("NRC_ENTER-ONOFF"), // ok button
    NRC_RETURN("NRC_RETURN-ONOFF"),
    NRC_SUBMENU("NRC_SUBMENU-ONOFF"), // options button

    NRC_3D("NRC_3D-ONOFF"),
    NRC_DISP_MODE("NRC_DISP_MODE-ONOFF"), // aspect button
    NRC_MENU("NRC_MENU-ONOFF"),
    NRC_EPG("NRC_EPG-ONOFF"), // guide button
    NRC_TEXT("NRC_TEXT-ONOFF"),
    NRC_STTL("NRC_STTL-ONOFF"),
    NRC_INFO("NRC_INFO-ONOFF"),
    NRC_GUIDE("NRC_GUIDE-ONOFF"), // e-help button

    NRC_D1("NRC_D1-ONOFF"),
    NRC_D2("NRC_D2-ONOFF"),
    NRC_D3("NRC_D3-ONOFF"),
    NRC_D4("NRC_D4-ONOFF"),
    NRC_D5("NRC_D5-ONOFF"),
    NRC_D6("NRC_D6-ONOFF"),
    NRC_D7("NRC_D7-ONOFF"),
    NRC_D8("NRC_D8-ONOFF"),
    NRC_D9("NRC_D9-ONOFF"),
    NRC_D0("NRC_D0-ONOFF"),
    NRC_R_TUNE("NRC_R_TUNE-ONOFF"), // last view button

    NRC_BLUE("NRC_BLUE-ONOFF"), // quick commands and teletext controls (based on context)
    NRC_YELLOW("NRC_YELLOW-ONOFF"),
    NRC_GREEN("NRC_GREEN-ONOFF"),
    NRC_RED("NRC_RED-ONOFF"),

    NRC_REW("NRC_REW-ONOFF"),
    NRC_PLAY("NRC_PLAY-ONOFF"),
    NRC_FF("NRC_FF-ONOFF"),
    NRC_SKIP_PREV("NRC_SKIP_PREV-ONOFF"),
    NRC_PAUSE("NRC_PAUSE-ONOFF"),
    NRC_SKIP_NEXT("NRC_SKIP_NEXT-ONOFF"),
    NRC_STOP("NRC_STOP-ONOFF"),
    NRC_REC("NRC_REC-ONOFF");

    private String value;

    private KeyCode() {
        value = null;
    }

    private KeyCode(String value) {
        this.value = value;
    }

    private KeyCode(KeyCode otherKey) {
        this(otherKey.getValue());
    }

    public String getValue() {
        if (value == null) {
            return this.name();
        }
        return value;
    }
}
