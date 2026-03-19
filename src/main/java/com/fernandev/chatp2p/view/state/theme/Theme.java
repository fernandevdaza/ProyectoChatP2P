package com.fernandev.chatp2p.view.state.theme;

import com.fernandev.chatp2p.view.state.theme.leftpanel.LeftPanelTheme;
import com.fernandev.chatp2p.view.state.theme.rightpanel.RightPanelTheme;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Theme {
    RightPanelTheme rightPanelTheme = new RightPanelTheme();
    LeftPanelTheme leftPanelTheme = new LeftPanelTheme();;

    public Theme(){
    }
}
