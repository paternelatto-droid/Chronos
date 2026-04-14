package com.latto.chronos.response;

import com.latto.chronos.models.PastorAvailability;
import java.util.List;

public class PastorAvailabilityResponse {
    private boolean success;
    private List<PastorAvailability> availability;

    public boolean isSuccess() { return success; }
    public List<PastorAvailability> getAvailability() { return availability; }
}
