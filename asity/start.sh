# this is a hack way to set the snowflake node id base on the stateful set ordinal
pod_index=${HOSTNAME##*-}
export SNOWFLAKE_NODEID=$pod_index

export
java -Xms2g -Xmx2g -cp app:app/lib/* com.keepreal.madagascar.asity.AsityApplication