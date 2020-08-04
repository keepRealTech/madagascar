package com.keepreal.madagascar.lemur.converter;

import com.keepreal.madagascar.common.MediaType;
import swagger.model.MultiMediaType;

public class MediaTypeConverter {

    public static MediaType convert(MultiMediaType type) {
        switch (type) {
            case MULTIMEDIA_TYPE_TEXT:
                return MediaType.MEDIA_TEXT;
            case MULTIMEDIA_TYPE_PICS:
                return MediaType.MEDIA_PICS;
            case MULTIMEDIA_TYPE_ALBUM:
                return MediaType.MEDIA_ALBUM;
            case MULTIMEDIA_TYPE_VIDEO:
                return MediaType.MEDIA_VIDEO;
            case MULTIMEDIA_TYPE_AUDIO:
                return MediaType.MEDIA_AUDIO;
            case MULITMEDIA_TYPE_HTML:
                return MediaType.MEDIA_HTML;
            default:
                return MediaType.MEDIA_NONE;
        }
    }
}
