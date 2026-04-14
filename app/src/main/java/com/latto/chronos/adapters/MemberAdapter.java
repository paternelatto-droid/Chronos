package com.latto.chronos.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.latto.chronos.R;
import com.latto.chronos.api.ApiClient;
import com.latto.chronos.api.ApiService;
import com.latto.chronos.models.Member;
import com.latto.chronos.models.UserSession;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {

    // =================== INTERFACE CALLBACK ===================
    public interface MemberActionListener {
        void onEdit(Member member);
        void onPlanning(Member member);
    }

    // =================== VARIABLES ===================
    private final Context context;
    private List<Member> members;
    private List<Member> membersFull; // liste complète pour la recherche
    private final MemberActionListener listener;

    private String activeRoleFilter = "";   // filtre rôle actuel
    private String activeSearchQuery = "";  // filtre recherche actuel

    // =================== CONSTRUCTEUR ===================
    public MemberAdapter(Context ctx, List<Member> list, MemberActionListener listener) {
        this.context = ctx;
        this.members = list;
        this.membersFull = new ArrayList<>(members);
        this.listener = listener;
    }

    // =================== MÉTHODE POUR METTRE À JOUR LA LISTE ===================
    public void setData(List<Member> newMembers) {
        this.members.clear();
        this.members.addAll(newMembers);
        this.membersFull.clear();
        this.membersFull.addAll(newMembers);
        notifyDataSetChanged();
    }

    // =================== CREATION VIEWHOLDER ===================
    @NonNull
    @Override
    public MemberAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_member, parent, false);
        return new ViewHolder(v);
    }

    // =================== BIND VIEWHOLDER ===================
    @Override
    public void onBindViewHolder(@NonNull MemberAdapter.ViewHolder holder, int position) {
        Member m = members.get(position);

        holder.tvName.setText((m.getFullName().isEmpty() ? ("#" + m.getId()) : m.getFullName()));
        holder.tvPhone.setText(m.getPhone() != null && !m.getPhone().isEmpty() ? m.getPhone() : "—");

        String avatarText = m.getFirst_name() != null && !m.getFirst_name().isEmpty()
                ? m.getFirst_name().substring(0, 1).toUpperCase() : "?";
        holder.tvAvatarLetter.setText(avatarText);

        // Clic sur le bouton options
        holder.optionButton.setOnClickListener(v -> showOptionsMenu(v, m));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    // =================== MENU OPTIONS ===================
    private void showOptionsMenu(View anchor, Member member) {
        PopupMenu popup = new PopupMenu(context, anchor);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_members_actions, popup.getMenu());

        // Vérifier permissions
        if (!UserSession.hasPermission("member.update")) {
            popup.getMenu().findItem(R.id.action_edit_member).setVisible(false);
        }
        if (!UserSession.hasPermission("member.delete")) {
            popup.getMenu().findItem(R.id.action_delete_member).setVisible(false);
        }
        if (!UserSession.hasPermission("member.planning")) {
            if (member.getRole_id() != 4)
                popup.getMenu().findItem(R.id.action_planning_member).setVisible(false);
        }

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit_member) {
                listener.onEdit(member); // callback vers le fragment
                return true;
            } else if (id == R.id.action_planning_member) {
                listener.onPlanning(member); // callback vers le fragment
                return true;
            } else if (id == R.id.action_delete_member) {
                // Confirmer la suppression
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Supprimer membre")
                        .setMessage("Voulez-vous vraiment supprimer ce membre ?")
                        .setPositiveButton("Oui", (dialog, which) -> deleteMember(member))
                        .setNegativeButton("Non", null)
                        .show();
                return true;
            }
            return false;
        });

        popup.show();
    }

    // =================== FILTRE PAR ROLE ===================
    public void filterByRole(String roleName) {
        activeRoleFilter = roleName != null && !roleName.equalsIgnoreCase("Tous") ? roleName.toLowerCase() : "";
        applyFilters();
    }

    // =================== FILTRE PAR TEXTE ===================
    public void filter(String query) {
        activeSearchQuery = query.toLowerCase().trim();
        applyFilters();
    }

    private void applyFilters() {
        List<Member> filtered = new ArrayList<>();
        for (Member m : membersFull) {
            boolean matchText = activeSearchQuery.isEmpty() ||
                    (m.getFullName() != null && m.getFullName().toLowerCase().contains(activeSearchQuery)) ||
                    (m.getPhone() != null && m.getPhone().toLowerCase().contains(activeSearchQuery)) ||
                    (m.getEmail() != null && m.getEmail().toLowerCase().contains(activeSearchQuery));

            boolean matchRole = activeRoleFilter.isEmpty() ||
                    (m.getRole_name() != null && m.getRole_name().toLowerCase().equals(activeRoleFilter));

            if (matchText && matchRole) filtered.add(m);
        }
        members.clear();
        members.addAll(filtered);
        notifyDataSetChanged();
    }

    // =================== SUPPRESSION MEMBRE ===================
    private void deleteMember(Member member) {
        ApiService api = ApiClient.getService(context);
        Call<Void> call = api.deleteMember(member.getId());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Membre supprimé avec succès", Toast.LENGTH_SHORT).show();
                    members.remove(member);
                    membersFull.remove(member);
                    notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Connexion échouée: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =================== VIEWHOLDER ===================
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvAvatarLetter;
        ImageView imgAvatar;
        ImageButton optionButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.txtMemberName);
            tvPhone = itemView.findViewById(R.id.txtMemberPhone);
            tvAvatarLetter = itemView.findViewById(R.id.txtAvatarLetter);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            optionButton = itemView.findViewById(R.id.optionButton);
        }
    }
}
