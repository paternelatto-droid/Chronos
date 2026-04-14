package com.latto.chronos.api;

import com.latto.chronos.models.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;


import com.latto.chronos.models.Event;
import com.latto.chronos.models.EventUser;
import com.latto.chronos.models.Notification;
import com.latto.chronos.models.RecurringPattern;
import com.latto.chronos.response.EventResponse;
import com.latto.chronos.response.MemberResponse;
import com.latto.chronos.response.MemberWithUserGetResponse;
import com.latto.chronos.response.MemberWithUserResponse;
import com.latto.chronos.response.PastorAvailabilityResponse;
import com.latto.chronos.response.ReminderResponse;
import com.latto.chronos.response.RoleResponse;
import com.latto.chronos.response.SimpleResp;
import com.latto.chronos.models.User;
import com.latto.chronos.response.AvailabilityResponse;
import com.latto.chronos.response.LoginResponse;


public interface ApiService {

    @GET("ping.php") // fichier PHP simple côté serveur qui renvoie { "success": true }
    Call<Void> pingServer();

    @POST("login.php")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Events
    @GET("event_all.php")
    Call<EventResponse> getEvents(@Query("user_id") int userId);


    @POST("event_create.php")
    Call<SimpleResp> createEvent(@Body Event event);

    @PUT("event_update.php") // ou le endpoint que tu utilises pour update
    Call<SimpleResp> updateEvent(@Query("id") int id, @Body Event event);


    @DELETE("event_delete.php")
    Call<SimpleResp> deleteEvent(
            @Query("id") int eventId
    );


    // ✅ Récupération des types d'événements
    @GET("event_types.php")
    Call<List<EventType>> getEventTypes();

    // Users
    @GET("users.php")
    Call<List<User>> getUsers();

    @GET("is_pastor_available.php")
    Call<AvailabilityResponse> isUserAvailable(@Query("user_id") int userId,
                                               @Query("event_datetime") String event_datetime);

    // inside interface ApiService
    @GET("members_list.php")
    Call<MemberResponse> getMembers();

    @POST("members_create.php")
    Call<Member> createMember(@Body Member member);
    @GET("members_get_user.php")
    Call<MemberWithUserGetResponse> getMemberWithUser(@Query("member_id") int memberId);

    @POST("members_update_user.php")
    Call<MemberWithUserResponse> updateMemberWithUser(@Body MemberWithUserRequest request);

    // ✅ Création d'un membre avec option création user
    @POST("members_create_user.php")
    Call<MemberWithUserResponse> createMemberWithUser(@Body MemberWithUserRequest request);

    @FormUrlEncoded
    @POST("members_delete.php")
    Call<Void> deleteMember(@Field("id") int memberId);

    @GET("role_list.php")
    Call<RoleResponse> getRoles();

    // get list
    @GET("pastor_availability_list.php")
    Call<PastorAvailabilityResponse> getPastorAvailability(@Query("user_id") int userId);

    // add
    @POST("pastor_availability_add.php")
    Call<SimpleResp> addPastorAvailability(@Body PastorAvailabilityRequest request);

    // delete
    @POST("pastor_availability_delete.php")
    Call<SimpleResp> deletePastorAvailability(@Body IdRequest request);

    @GET("check_reminders.php")
    Call<ReminderResponse> checkReminders(@Query("user_id") int userId);
    @POST("reminders_mark_sent.php")
    Call<Void> markReminderAsSent(@Query("id") int id);


}
