package com.keepreal.madagascar.workflow.statistics.model.coua;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IslandIncrement {

    private String islandId;
    private BigInteger increment;

}
