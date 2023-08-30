package net.sourceforge.stripes.controller;

import sk.iway.iwcm.PageLng;
import sk.iway.iwcm.Tools;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 *  IwayUtils.java
 *
 *@Title        webjet5
 *@Company      Interway s.r.o. (www.interway.sk)
 *@Copyright    Interway s.r.o. (c) 2001-2007
 *@author       $Author: jeeff $
 *@version      $Revision: 1.2 $
 *@created      Date: 27.3.2007 11:49:57
 *@modified     $Date: 2007/08/06 13:15:30 $
 */
public class IwayStripesUtils
{
	public static void setLocale(HttpServletRequest request, String lng)
	{
		if (request.getLocale() != null && request.getLocale().getLanguage().equalsIgnoreCase(lng)) return;

		if (request instanceof StripesRequestWrapper)
		{
			//aby sme mali normalne datumy
			//if ("en".equals(lng)) lng = "en_GB";
			//if ("en".equals(lng)) lng = "sk";

			//inak boli problemy s formatmi datumov
			//lng = "sk";

			StripesRequestWrapper sRequest = (StripesRequestWrapper)request;

			String isoLocale[] = Tools.getTokens(PageLng.getUserLngIso(lng), "-");
			if (isoLocale.length==2)
			{
				sRequest.setLocale(new Locale(isoLocale[0], isoLocale[1]));
			}
		}
	}
}
