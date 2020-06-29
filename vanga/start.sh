# this is a hack way to set the snowflake node id base on the stateful set ordinal
pod_index=${HOSTNAME##*-}
export SNOWFLAKE_NODEID=$pod_index
export WECHATPAY_HOSTIP=$(dig +short myip.opendns.com @resolver1.opendns.com)

export
java -Xms2g -Xmx2g -cp app:app/lib/* com.keepreal.madagascar.vanga.VangaApplication