package org.MakeACakeStudios.donates;

import org.MakeACakeStudios.donates.effects.BirthdayEffect;
import org.MakeACakeStudios.donates.effects.SakuraLeavesEffect;
import org.MakeACakeStudios.donates.effects.StarEffect;
import org.MakeACakeStudios.donates.effects.ZeusEffect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EffectManager {

    private static final Map<String, Integer> DONATE_EFFECTS = new HashMap<>() {{
        put("Zeus", 1);
        put("Star", 2);
        put("Sakura", 3);
        put("Iam", 11);
        put("Birthday", 12);
        put("Vanila", 13);
    }};

    private static final Map<String, Set<Integer>> enabledEffects = new HashMap<>();

    public static void startEffectForDonation(int donationId, String playerName) {
        switch (donationId) {
            case 1 -> ZeusEffect.startEffect(playerName);
            case 2 -> StarEffect.startEffect(playerName);
            case 3 -> SakuraLeavesEffect.startEffect(playerName);
            case 12 -> BirthdayEffect.startEffect(playerName);
        }
        enabledEffects.computeIfAbsent(playerName, k -> new HashSet<>()).add(donationId);
    }

    public static void stopEffectForDonation(int donationId, String playerName) {
        switch (donationId) {
            case 1 -> ZeusEffect.stopEffect(playerName);
            case 2 -> StarEffect.stopEffect(playerName);
            case 3 -> SakuraLeavesEffect.stopEffect(playerName);
            case 12 -> BirthdayEffect.stopEffect(playerName);
        }
        Set<Integer> effects = enabledEffects.get(playerName);
        if (effects != null) {
            effects.remove(donationId);
            if (effects.isEmpty()) {
                enabledEffects.remove(playerName);
            }
        }
    }

    public static Set<Integer> getEnabledEffectsForPlayer(String playerName) {
        return enabledEffects.getOrDefault(playerName, Set.of());
    }

    public static Set<Integer> getDisabledEffectsForPlayer(String playerName) {
        Set<Integer> enabled = getEnabledEffectsForPlayer(playerName);
        Set<Integer> allEffects = new HashSet<>(DONATE_EFFECTS.values());
        allEffects.removeAll(enabled);
        return allEffects;
    }
}
