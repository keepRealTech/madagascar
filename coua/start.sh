# this is a hack way to set the snowflake node id base on the stateful set ordinal
pod_index=${HOSTNAME##*-}
export SNOWFLAKE_NODEID=$pod_index

export
java -cp app:app/lib/* com.keepreal.madagascar.coua.CouaApplication