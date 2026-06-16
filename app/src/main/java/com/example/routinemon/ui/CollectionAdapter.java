package com.example.routinemon.ui;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.routinemon.R;

import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {
    private List<Monster> monsterList;

    public CollectionAdapter(List<Monster> list) {
        this.monsterList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_monster, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Monster m = monsterList.get(position);

        holder.tvMonsterName.setTextSize(13);
        holder.tvMonsterName.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = (int) (8 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
        holder.tvMonsterName.setLayoutParams(lp);

        // 🔓 [해금 완료]
        if (m.isUnlocked) {
            holder.ivMonster.setImageResource(m.imageRes);
            holder.ivMonster.clearColorFilter();
            holder.ivMonster.setImageAlpha(255);
            holder.tvMonsterName.setText(m.name);
            holder.tvMonsterName.setTextColor(Color.parseColor("#333333"));
        }
        // 🔒 [미해금 흑화 그림자]
        else {
            holder.ivMonster.setImageResource(m.imageRes);
            holder.ivMonster.setColorFilter(Color.parseColor("#222222"), PorterDuff.Mode.SRC_IN);
            holder.ivMonster.setImageAlpha(200);
            holder.tvMonsterName.setText("???");
            holder.tvMonsterName.setTextColor(Color.parseColor("#A0A0A0"));
        }

        // 🛎️ 클릭 이벤트 및 이스터에그 멘트
        holder.itemView.setOnClickListener(v -> {
            boolean isUserTeenager = monsterList.get(2).isUnlocked;

            if (m.isUnlocked) {
                String description = "";
                if (m.name.contains("베이비")) {
                    description = "갓 태어난 말랑말랑한 베이비 뽀뽀입니다. 물 한 잔 마시기 같은 기초적인 루틴을 통해 성장할 준비를 하고 있습니다.";
                } else if (m.name.contains("어린이")) {
                    description = "매일 루틴을 성실히 실천해 각성한 어린이 뽀뽀입니다! 몸집이 커지고 장난기가 많아졌으며, 주인을 보면 폴짝폴짝 뜁니다.";
                } else if (m.name.contains("청소년")) {
                    description = "질풍노도의 시기를 건강한 루틴으로 극복한 청소년 뽀뽀입니다. 늠름한 자태를 뽐내며 주인을 도와줄 준비를 합니다.";
                }

                new AlertDialog.Builder(v.getContext())
                        .setTitle("🦖 " + m.name)
                        .setMessage("\n" + description)
                        .setPositiveButton("확인", null)
                        .show();
            }
            else {
                if (m.name.contains("각성 지룡") || m.name.contains("타락 비룡") || m.name.contains("전설의 신룡")) {

                    String dialogTitle = "🔒 베일에 싸인 루틴몬";
                    String specialMessage = "";

                    if (!isUserTeenager) {
                        specialMessage = "아직 베일에 싸인 전설의 루틴몬입니다.\n더 깊은 차원의 루틴을 마스터해야 그 실루엣을 마주할 수 있을 것 같습니다.";
                    } else {
                        dialogTitle = "⚡ 전설의 속삭임 (" + m.name + ")";

                        if (m.name.contains("각성 지룡")) {
                            specialMessage = "\"대지에 발을 붙이고 매일 같은 시간 일어나는 자여...\n흔들리지 않는 대지의 기운이 그대의 루틴 속에 깃들어 있도다.\"\n\n(대지의 각성을 이루려면 더 혹독한 루틴 연속 성공이 필요해 보입니다.)";
                        } else if (m.name.contains("타락 비룡")) {
                            specialMessage = "\"번아웃과 게으름의 어둠을 삼키고 날아오른 파괴의 날개...\n금기를 깨고 루틴의 극한을 맛본 자만이 나를 제어할 수 있다.\"\n\n(어두운 슬럼프를 극복하는 순간 해금될 강력한 이계의 존재입니다.)";
                        } else if (m.name.contains("전설의 신룡")) {
                            specialMessage = "\"시간과 공간을 초월해 영원의 지평선에 도달한 마스터여.\n내 안의 근원의 힘이 깨어날 때, 당신의 루틴은 비로소 완벽한 신화가 된다.\"\n\n(모든 커스텀 루틴을 마스터한 진정한 '루틴 군주'에게만 허락된 최종 신화 개체입니다.)";
                        }
                    }

                    new AlertDialog.Builder(v.getContext())
                            .setTitle(dialogTitle)
                            .setMessage("\n" + specialMessage)
                            .setPositiveButton("경배하기", null)
                            .show();

                } else {
                    Toast.makeText(v.getContext(), "🔒 아직 만나지 못한 루틴몬입니다. 루틴을 완료해 진화시켜 보세요!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return monsterList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMonster;
        TextView tvMonsterName;

        public ViewHolder(@NonNull View iv) {
            super(iv);
            ivMonster = iv.findViewById(R.id.ivMonster);
            LinearLayout container = iv.findViewById(R.id.layoutContainer);
            tvMonsterName = new TextView(iv.getContext());
            if (container != null) {
                container.addView(tvMonsterName);
            }
        }
    }
}