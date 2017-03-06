package net.sourceforge.stripes.tag.layout;

import org.testng.annotations.Test;

import java.nio.ByteBuffer;

/**
 * Created by hst92 on 27.02.2017.
 */
public class MyServletOutputStreamTest {

    @Test
    public void shouldWriteSuccess() throws Exception {
        MyServletOutputStream stream = new MyServletOutputStream(new DummyJspWriter(4080, true));
        String text = "This is an example é◌";
        StringBuffer buffer = new StringBuffer();
        for (int i=0;i<4080;i=buffer.length()) {
            buffer.append(text);
        }
        byte[] bytes = buffer.toString().substring(0, 4080).getBytes();
        stream.write(bytes, 0, 4080);
    }

    @Test
    public void shouldWriteFailArraySize() throws Exception {
        MyServletOutputStream stream = new MyServletOutputStream(new DummyJspWriter(4080, true));
        String text = "é◌";
        StringBuffer buffer = new StringBuffer();
        for (int i=0;i<4080;i=buffer.length()) {
            buffer.append(text);
        }
        byte[] bytes = buffer.toString().substring(0, 4080).getBytes();
        stream.write(bytes, 0, 4080);
    }

    @Test
    public void shouldWriteFailRemainingNull() throws Exception {
        MyServletOutputStream stream = new MyServletOutputStream(new DummyJspWriter(4080, true));
        stream.bbuf = ByteBuffer.allocate(0);
        String text = "Some Text é◌";
        StringBuffer buffer = new StringBuffer();
        for (int i=0;i<4080;i=buffer.length()) {
            buffer.append(text);
        }
        byte[] bytes = buffer.toString().substring(0, 4080).getBytes();
        stream.write(bytes, 0, 4080);
    }

    @Test
    public void shouldWriteFailNull2() throws Exception {
        MyServletOutputStream stream = new MyServletOutputStream(new DummyJspWriter(4080, true));
        String text = "4164\">21031 Hamburg, BodestraÃŸe 1</option><option value=\"859158\">21031 Hamburg, LohbrÃ¼gger Landstrasse 6</option><option value=\"874167\">21031 Hamburg, LohbrÃ¼gger Landstrasse 6</option><option value=\"871210\">21033 Hamburg, Ulmenliet 20</option><option value=\"858100\">21035 Hamburg, Fleetplatz 2</option><option value=\"859152\">21035 Hamburg, Walter Rudolphi Weg 5</option><option value=\"874162\">21035 Hamburg, Walter Rudolphi Weg 5</option><option value=\"762715\">2105 VE Heemstede, Amstellaan 10</option><option value=\"739510\">2105 VE Heemstede, Amstellaan UNKNOWN</option><option value=\"862160\">2106 AJ Heemstede, Vondelkade 2</option><option value=\"863101\">2106 AJ Heemstede, Vondelkade 2</option><option value=\"873151\">2106 AJ Heemstede, Vondelkade 2</option><option value=\"856150\">2106 BE Heemstede, Irislaan 11</option><option value=\"835108\">21073 Hamburg, GroÃŸer Schippsee 18</option><option value=\"874106\">21073 Hamburg, GroÃŸer Schippsee 18</option><option value=\"11178\">21073 Hamburg, GroÃŸer Schippsee 18, gegenÃ¼ber Hs.18</option><option value=\"870145\">21073 Hamburg, Julius-Ludowig Strasse 32</option><option value=\"8067\">21073 Hamburg, Sand 13</option><option value=\"874109\">21073 Hamburg, Sand 13</option><option value=\"835115\">21073 Hamburg, Sand 13</option><option value=\"875114\">21073 Hamburg, Sand 13</option><option value=\"874104\">21073 Hamburg, SchlossmÃ¼hlendamm 23</option><option value=\"863100\">21073 Hamburg, SchlossmÃ¼hlendamm 23</option><option value=\"9051\">21073 Hamburg, SchwarzenbergstraÃŸe 95</option><option value=\"862166\">21075 Hamburg, Nobleestrasse 3</option><option value=\"874100\">21075 Hamburg, SchwarzenbergstraÃŸe 95</option><option value=\"851193\">21075 Hamburg, SchwarzenbergstraÃŸe 95</option><option value=\"834101\">21075 Hamburg, SchwarzenbergstraÃŸe 95</option><option value=\"836140\">21075 Hamburg, SchwarzenbergstraÃŸe 95</option><option value=\"735051\">21079 Hamburg, Harburger SchloÃŸstraÃŸe 6</option><option value=\"5110\">21079 Hamburg, HÃ¶rstener Strasse 49</option><option value=\"849151\">21079 Hamburg, HÃ¶rstener StraÃŸe 49</option><option value=\"735050\">21079 Hamburg, Veritaskai 2a</option><option value=\"874102\">21079 Hamburg, Veritaskai 3</option><option value=\"861162\">21079 Hamburg, Veritaskai 3</option><option value=\"11179\">21079 Hamburg, Zum Handwerkszentrum 1</option><option value=\"835109\">21079 Hamburg, Zum Handwerkszentrum 1</option><option value=\"874110\">21079 Hamburg, Zum Handwerkszentrum 1</option><option value=\"809128\">21079 Hamburg, Zum Handwerkszentrum 1</option><option value=\"11150\">21079 Hamburg, Zum Handwerkszentrum 1, Handwerkskammer</option><option value=\"885107\">2111GM Aerdenhout, Spechtlaan 17</option><option value=\"861102\">2111XP Aerdenhout, Rijnegomlaan 64</option><option value=\"8060\">21129 Hamburg, Am Ballinkai 1, HHLA CTA</option><option value=\"8059\">21129 Hamburg, Burchardkai 1, HHLA CTB</option><option value=\"798121\">21129 Hamburg, Hein-Sass-Weg 30</option><option value=\"5118\">21129 Hamburg, Kreetslag 10, Airbus, F17 Halle 9, F18 Halle 249</option><option value=\"5106\">21129 Hamburg, Rugenberger Damm 1, MVR</option><option value=\"820101\">2116 EG Bentveld, Bentveldweg 1</option><option value=\"772058\">2121AK Bennebroek, J.F. van Lieroppark 12</option><option value=\"762917\">2131 CR Hoofddorp, Kruisweg 1023</option><option value=\"820140\">2131GE Hoofddorp, Soderblomstraat 169</option><option value=\"852105\">2131PR Hoofddorp, Wilsonstraat 215</option><option value=\"828102\">2131RV Hoofddorp, Kaj Munkweg 45</option><option value=\"828104\">2131ZK Hoofddorp, Schermerstraat 61</option><option value=\"762915\">2132 HB Hoofddorp, Saturnusstraat 25G</option><option value=\"762936\">2132 KG Hoofddorp, Hendrik Andriessenlaan 1</option><option value=\"762914\">2132 VZ Hoofddorp, Saffierlaan 4</option><option value=\"739779\">2132 WX Hoofddorp, Robijnlaan 4</option><option value=\"739788\">2132 WZ Hoofddorp, Saffierlaan 4</option><option value=\"762932\">2132 XZ Hoofddorp, Jadelaan 157</option><option value=\"762935\">2132 ZP Hoofddorp, Klaas van Reeuwijkstraat 60</option><option value=\"852103\">2132";
        byte[] bytes = text.toString().getBytes();
        byte[] extend = new byte[4080];
        int j = 0;
        for (int i=0;i<bytes.length;i++) {
            if (i<bytes.length) {
                if (bytes[i] == -61 && bytes[i+1] == -125) {
                    extend[j] = -61;
                    i++;
                }
                else if (bytes[i] == -59 && bytes[i+1] == -72) {
                    extend[j] = -97;
                    i++;
                }
                else if (bytes[i] == -61 && bytes[i+1] == -125) {
                    extend[j] = -61;
                    i++;
                }
                else if (bytes[i] == -62 && bytes[i+1] == -68) {
                    extend[j] = -68;
                    i++;
                }
                else if (bytes[i] == -62 && bytes[i+1] == -74) {
                    extend[j] = -74;
                    i++;
                }
                else {
                    extend[j] = bytes[i];
                }
                j++;
            }
        }
        stream.write(extend, 0, 4080);
    }

    @Test
    public void shouldWriteSuccessNullLength632() throws Exception {
        MyServletOutputStream stream = new MyServletOutputStream(new DummyJspWriter(4080, true));
        int max = 632;
        byte[] extend = new byte[max];
        for (int i=0;i<max;i++) {
            extend[i] = -61;
        }
        stream.write(extend, 0, max);
    }

    @Test
    public void shouldWriteFailNullLength633() throws Exception {
        MyServletOutputStream stream = new MyServletOutputStream(new DummyJspWriter(4080, true));
        int max = 633;
        byte[] extend = new byte[max];
        for (int i=0;i<max;i++) {
            extend[i] = -61;
        }
        stream.write(extend, 0, max);
    }
}
