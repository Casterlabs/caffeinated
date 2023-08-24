package co.casterlabs.caffeinated.builtin;

import java.util.List;
import java.util.stream.Collectors;

import co.casterlabs.caffeinated.builtin.widgets.goals.CustomGoal;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class GoalsService {
    private CaffeinatedDefaultPlugin plugin;

    public List<Widget> getAllGoals() {
        return this.plugin.getWidgets()
            .stream()
            .filter((w) -> w instanceof CustomGoal)
            .collect(Collectors.toList());
    }

    public void increment(@NonNull String id) {
        CustomGoal goal = (CustomGoal) this.plugin.getWidgets()
            .stream()
            .filter((w) -> w instanceof CustomGoal)
            .filter((w) -> w.getId().equals(id))
            .findFirst()
            .get();
        goal.update(goal.getCount() + 1);
    }

    public void decrement(@NonNull String id) {
        CustomGoal goal = (CustomGoal) this.plugin.getWidgets()
            .stream()
            .filter((w) -> w instanceof CustomGoal)
            .filter((w) -> w.getId().equals(id))
            .findFirst()
            .get();
        goal.update(goal.getCount() - 1);
    }

    public void set(@NonNull String id, double value) {
        CustomGoal goal = (CustomGoal) this.plugin.getWidgets()
            .stream()
            .filter((w) -> w instanceof CustomGoal)
            .filter((w) -> w.getId().equals(id))
            .findFirst()
            .get();
        goal.update(value);
    }

}
