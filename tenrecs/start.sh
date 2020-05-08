# this is a hack way to set the snowflake node id base on the stateful set ordinal
pod_index=${POD_NAME##*-}
export SNOWFLAKE_NODEID=$pod_index

export
java -cp app:app/lib/* com.keepreal.madagascar.tenrecs.TenrecsApplication