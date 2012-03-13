package net.msites.kaffeekasse;

import net.msites.guilibrary.toolbox.GUIHelper;
import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.kaffeekasse.ui.mainframe.KaffeekasseFrame;

public class KaffeekasseApplication {

	public static void main(String[] args) throws Exception {
		GUIHelper.useJGoodiesLooks();
		GUIImageManager.loadImageManager("/net/msites/kaffeekasse/ui/res/");
		
		new KaffeekasseFrame().setVisible(true);
	}

}