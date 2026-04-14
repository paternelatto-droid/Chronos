package com.latto.chronos.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.latto.chronos.R;
import com.latto.chronos.adapters.MemberAdapter;
import com.latto.chronos.api.ApiClient;
import com.latto.chronos.api.ApiService;
import com.latto.chronos.models.Member;
import com.latto.chronos.models.Role;
import com.latto.chronos.models.UserSession;
import com.latto.chronos.response.MemberResponse;
import com.latto.chronos.response.RoleResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MembreFragment extends Fragment {

    private RecyclerView recyclerMembers;
    private TextView tvMembersEmpty;
    private MemberAdapter adapter;
    private List<Member> memberList = new ArrayList<>();
    private FloatingActionButton fabAddMember;
    private List<Role> roleList = new ArrayList<>();
    private ApiService api;

    public MembreFragment() {
        setHasOptionsMenu(true);
    }

    // =================== CALLBACKS ADAPTER ===================
    private final MemberAdapter.MemberActionListener memberActionListener = new MemberAdapter.MemberActionListener() {
        @Override
        public void onEdit(Member member) {
            Intent intent = new Intent(getContext(), UpdateMemberActivity.class);
            intent.putExtra("member_id", member.getId());
            editMemberLauncher.launch(intent);
        }

        @Override
        public void onPlanning(Member member) {
            Intent intent = new Intent(getContext(), PastorAvailabilityActivity.class);
            intent.putExtra("user_id", member.getUser_id());
            startActivity(intent);
        }
    };

    // =================== ACTIVITY RESULT LAUNCHERS ===================
    private final ActivityResultLauncher<Intent> addMemberLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK) {
                    loadMembers(); // rafraîchir la liste après ajout
                }
            });

    private final ActivityResultLauncher<Intent> editMemberLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK) {
                    loadMembers(); // rafraîchir la liste après modification
                }
            });

    // =================== ONCREATEVIEW ===================
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_membre, container, false);
        api = ApiClient.getService(getContext());

        // Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbarMembers);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        recyclerMembers = view.findViewById(R.id.recyclerMembers);
        tvMembersEmpty = view.findViewById(R.id.tvMembersEmpty);
        fabAddMember = view.findViewById(R.id.fabAddMember);

        recyclerMembers.setLayoutManager(new LinearLayoutManager(getContext()));

        if (UserSession.hasPermission("member.create")) {
            fabAddMember.setVisibility(View.VISIBLE);
        } else {
            fabAddMember.setVisibility(View.GONE);
        }

        adapter = new MemberAdapter(getContext(), memberList, memberActionListener);
        recyclerMembers.setAdapter(adapter);

        loadMembers(); // Charger membres
        loadRoles();   // Charger rôles pour filtrage

        fabAddMember.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddMemberActivity.class);
            addMemberLauncher.launch(intent);
        });

        return view;
    }

    // =================== CHARGER MEMBRES ===================
    private void loadMembers() {
        api.getMembers().enqueue(new Callback<MemberResponse>() {
            @Override
            public void onResponse(Call<MemberResponse> call, Response<MemberResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    //memberList.clear();
                    //memberList.addAll(response.body().getMembers());
                    adapter.setData(response.body().getMembers());
                    toggleEmptyView(adapter.getItemCount());
                }
            }

            @Override
            public void onFailure(Call<MemberResponse> call, Throwable t) {
                toggleEmptyView(0);
            }
        });
    }

    private void toggleEmptyView(int itemCount) {
        if (itemCount == 0) {
            tvMembersEmpty.setVisibility(View.VISIBLE);
            recyclerMembers.setVisibility(View.GONE);
        } else {
            tvMembersEmpty.setVisibility(View.GONE);
            recyclerMembers.setVisibility(View.VISIBLE);
        }
    }

    // =================== MENU RECHERCHE ===================
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_members_toolbar, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Rechercher par nom ou numéro...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    // =================== FILTRE PAR ROLE ===================
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_filter_role) {
            showRoleFilterDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showRoleFilterDialog() {
        if (roleList.isEmpty()) {
            Toast.makeText(getContext(), "Aucun rôle trouvé", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> roleNames = new ArrayList<>();
        roleNames.add("Tous");
        for (Role r : roleList) roleNames.add(r.getName());

        String[] rolesArray = roleNames.toArray(new String[0]);

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Filtrer par rôle")
                .setItems(rolesArray, (dialog, which) -> {
                    String selectedRole = rolesArray[which];
                    adapter.filterByRole(selectedRole);
                })
                .show();
    }

    private void loadRoles() {
        api.getRoles().enqueue(new Callback<RoleResponse>() {
            @Override
            public void onResponse(Call<RoleResponse> call, Response<RoleResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    roleList = response.body().getRoles();
                } else {
                    Toast.makeText(getContext(), "Erreur lors du chargement des rôles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RoleResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Impossible de charger les rôles", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =================== RECHARGER AU RETOUR DU FRAGMENT ===================
    @Override
    public void onResume() {
        super.onResume();
        loadMembers();
    }
}
