package me.eddiep.ghost.ai.dna.conditions;

import me.eddiep.ghost.ai.dna.Condition;
import me.eddiep.ghost.ai.dna.ConditionType;
import me.eddiep.ghost.game.match.entities.PlayableEntity;

public class AllyTeamFiringCondition implements Condition {
    @Override
    public ConditionType type() {
        return ConditionType.Team;
    }

    @Override
    public Boolean run(PlayableEntity playableEntity) {
        for (PlayableEntity p : playableEntity.getAllies()) {
            if (p.isFiring())
                return true;
        }

        return false;
    }
}