package com.keepreal.madagascar.common.constants;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains constants.
 */
public class Constants {

    /**
     * The mock user id of public inbox.
     */
    public static final String PUBLIC_INBOX_USER_ID = "00000000";

    /**
     * The super user id for admin.
     */
    public static final String SUPER_ADMIN_USER_ID = "99999999";

    /**
     * The ios audit user ids.
     */
    public static final Set<String> AUDIT_USER_IDS = new HashSet<>(Collections.singleton("484"));

    /**
     * The official user id for marketing.
     */
    public static final String OFFICIAL_USER_ID = "4";

    /**
     * The comments count for feed.
     */
    public static final int DEFAULT_FEED_LAST_COMMENT_COUNT = 2;

    /**
     * The max length for membership name.
     */
    public static final int MEMBERSHIP_NAME_MAX_LENGTH = 20;

    /**
     * The default sponsor gift id.
     */
    public static final String DEFAULT_SPONSOR_GIFT_ID = "171";

    public static final String DEFAULT_SPONSOR_ISLAND_ID = "0";

    public static final String DEFAULT_SPONSOR_SKU_ISLAND_ID = "0";

    public static final String DEFAULT_SPONSOR_DESCRIPTION = "买杯椰子汁儿";

    public static final Long DEFAULT_SPONSOR_GIFT_UNIT_PRICE = 300L;

    public static final Long DEFAULT_SPONSOR_SKU_QUANTITY = 3L;

    /**
     * Redis key prefix
     */
    public static final String VIDEO_PREFIX = "video:";
}
