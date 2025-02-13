package org.MakeACakeStudios.donates;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public class EffectManager {
    private static final Map<String, Runnable> effects = new HashMap<>();

    /**
     * Регистрирует эффект в менеджере.
     * @param name Название эффекта
     * @param effect Запускаемый метод эффекта
     */
    public static void registerEffect(String name, Runnable effect) {
        effects.put(name, effect);
    }

    /**
     * Запускает все зарегистрированные эффекты.
     */
    public static void startAllEffects() {
        effects.values().forEach(Runnable::run);
    }

    /**
     * Запускает эффект для определённого игрока, если он зарегистрирован.
     * @param name Название эффекта
     */
    public static void startEffect(String name) {
        Runnable effect = effects.get(name);
        if (effect != null) {
            effect.run();
        }
    }
}
