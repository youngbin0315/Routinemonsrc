package com.example.routinemon.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.routinemon.R;
import com.example.routinemon.data.Routine;

import java.util.ArrayList;
import java.util.List;

public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder> {

    private List<Routine> routineList = new ArrayList<>();
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener; // ➕ 롱클릭 리스너 추가

    public interface OnItemClickListener {
        void onItemClick(Routine routine);
    }

    // ➕ 롱클릭 전용 인터페이스 정의
    public interface OnItemLongClickListener {
        void onItemLongClick(Routine routine);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // ➕ 롱클릭 리스너 설정 메서드
    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setRoutines(List<Routine> routines) {
        this.routineList = routines;
        notifyDataSetChanged();
    }

    public void resetAllRoutines() {
        if (routineList != null) {
            for (Routine r : routineList) {
                r.setCompleted(false);
            }
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_routine, parent, false);
        return new RoutineViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        Routine currentRoutine = routineList.get(position);

        if (holder.tvTitle != null) {
            holder.tvTitle.setText(currentRoutine.getTitle());
        }

        if (holder.tvStatus != null) {
            if (currentRoutine.isCompleted()) {
                holder.tvStatus.setText("완료");
            } else {
                holder.tvStatus.setText("진행 중");
            }
        }

        if (holder.ivCheckBtn != null) {
            if (currentRoutine.isCompleted()) {
                holder.ivCheckBtn.setImageResource(android.R.drawable.checkbox_on_background);
            } else {
                holder.ivCheckBtn.setImageResource(android.R.drawable.checkbox_off_background);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentRoutine);
            }
        });

        // ➕ 항목을 길게 누르면 실행될 이벤트 바인딩
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(currentRoutine);
                return true; // 이벤트를 여기서 소비함
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return routineList != null ? routineList.size() : 0;
    }

    static class RoutineViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvStatus;
        private final ImageView ivCheckBtn;

        public RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRoutineTitle);
            ivCheckBtn = itemView.findViewById(R.id.ivCheckStatus);
            tvStatus = itemView.findViewById(R.id.tvRoutineStatus);
        }
    }
}