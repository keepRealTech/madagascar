package com.keepreal.madagascar.lemur.converter;

import com.keepreal.madagascar.common.MediaType;
import swagger.model.MultiMediaDTO;
import swagger.model.MultiMediaType;

public class MediaTypeConverter {

    public static MediaType convertToMediaType(MultiMediaType type) {
        switch (type) {
            case TEXT:
                return MediaType.MEDIA_TEXT;
            case PICS:
                return MediaType.MEDIA_PICS;
            case ALBUM:
                return MediaType.MEDIA_ALBUM;
            case VIDEO:
                return MediaType.MEDIA_VIDEO;
            case AUDIO:
                return MediaType.MEDIA_AUDIO;
            case HTML:
                return MediaType.MEDIA_HTML;
            case QUESTION:
                return MediaType.MEDIA_QUESTION;
            default:
                return MediaType.MEDIA_NONE;
        }
    }

    public static MultiMediaType converToMultiMediaType(MediaType type) {
        switch (type) {
            case MEDIA_TEXT:
                return MultiMediaType.TEXT;
            case MEDIA_PICS:
                return MultiMediaType.PICS;
            case MEDIA_ALBUM:
                return MultiMediaType.ALBUM;
            case MEDIA_VIDEO:
                return MultiMediaType.VIDEO;
            case MEDIA_AUDIO:
                return MultiMediaType.AUDIO;
            case MEDIA_HTML:
                return MultiMediaType.HTML;
            case MEDIA_QUESTION:
                return MultiMediaType.QUESTION;
            default:
                return MultiMediaType.TEXT;
        }
    }
}
