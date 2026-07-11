package edu.uptc.swii.sihope.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uptc.swii.sihope.dto.TimeBlock;

public class AvailabilityRequest {

    @JsonProperty("bloques")
    private List<TimeBlock> blocks;

    public List<TimeBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<TimeBlock> blocks) {
        this.blocks = blocks;
    }

}
