package com.hileone.animation.utils;

import com.hileone.animation.views.ShapePoint;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The creator is Leone && E-mail: butleone@163.com
 *
 * @author Leone
 * @date 5/10/16
 * @description Edit it! Change it! Beat it! Whatever, just do it!
 */
public class PullParseUtils {

    /**
     * Pull解析Shape xml文件，获取描点信息
     * @param stream 文件流
     * @param screenRatio 实际屏幕比例 width/720f
     * @return shape point list
     * @throws XmlPullParserException
     * @throws IOException
     */
    public static List<ShapePoint> parse(InputStream stream, float screenRatio) {
        List<ShapePoint> pointList = new ArrayList<>();

        try {
            XmlPullParser xmlParser = XmlPullParserFactory.newInstance().newPullParser();
            xmlParser.setInput(stream, "utf-8");
            int eventType = xmlParser.getEventType();
            float tDeltaX = 0;
            float tRatio = 1;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        String name = xmlParser.getName();
                        if (name.equals("points")) {
                            float deltaX = Float.valueOf(xmlParser.getAttributeValue(0));
                            float ratio = Float.valueOf(xmlParser.getAttributeValue(1));
                            tDeltaX = deltaX;
                            tRatio = ratio * screenRatio;
                        } else if (name.equals("point")) {
                            String attributeValue1 = xmlParser.getAttributeValue(0);
                            String attributeValue2 = xmlParser.getAttributeValue(1);
                            ShapePoint p = new ShapePoint(
                                    tRatio * (Float.valueOf(attributeValue1) + tDeltaX)
                                    , tRatio * Float.valueOf(attributeValue2));
                            pointList.add(p);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                    default:
                        break;
                }
                eventType = xmlParser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            pointList.clear();
        }
        return pointList;
    }

}
