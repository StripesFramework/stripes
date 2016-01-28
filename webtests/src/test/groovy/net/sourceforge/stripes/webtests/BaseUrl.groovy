package net.sourceforge.stripes.webtests

class BaseUrl {

    static String get() {
        System.getProperty('webtests.base.url', 'http://localhost:9999/webtests')
    }

}
