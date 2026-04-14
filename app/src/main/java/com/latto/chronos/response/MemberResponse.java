package com.latto.chronos.response;

import com.latto.chronos.models.Member;

import java.util.List;

public class MemberResponse {
    private boolean success;
    private List<Member> members;

    public boolean isSuccess() { return success; }
    public List<Member> getMembers() { return members; }
}
