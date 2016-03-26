package me.eddiep.ghost.ai.dna.movement;

import me.eddiep.ghost.ai.dna.AbstractSequence;
import me.eddiep.ghost.ai.dna.Condition;
import me.eddiep.ghost.ai.dna.ConditionType;
import me.eddiep.ghost.ai.dna.Sequence;
import me.eddiep.ghost.ai.dna.conditions.ConditionFactory;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.Vector2f;

public class RandomMovement extends AbstractSequence<Vector2f> {
    private float minX, minY, maxX, maxY;
    private Condition condition;

    public RandomMovement() {
        super();

        minX = Global.random(0f, 1024f);
        maxX = Global.random(0f, 720f);

        minY = Global.random(0f, 1024f);
        maxY = Global.random(0f, 720f);

        condition = ConditionFactory.getRandomCondition(ConditionType.Any);

        validate();
    }

    @Override
    public Vector2f execute(PlayableEntity owner) {
        if (condition.run(owner)) {
            float x = Global.random(minX, maxX);
            float y = Global.random(minY, maxY);

            return new Vector2f(x, y);
        }

        return null;
    }

    @Override
    public void mutate() {
        int temp = Global.random(0, 5);

        switch (temp) {
            case 0:
                minX = Global.random(0f, 1024f);
                break;
            case 1:
                minY = Global.random(0f, 720f);
                break;
            case 2:
                maxX = Global.random(0f, 1024f);
                break;
            case 3:
                maxY = Global.random(0f, 720f);
                break;
            case 5:
                condition = ConditionFactory.getRandomCondition(ConditionType.Any);
                break;
        }

        validate();

        super.mutate();
    }

    @Override
    public Sequence combine(Sequence sequence) {
        if (sequence instanceof RandomMovement) {
            RandomMovement randomMovement = (RandomMovement)sequence;

            RandomMovement newMovement = new RandomMovement();

            if (Global.RANDOM.nextBoolean())
                newMovement.minX = minX;
            else
                newMovement.minX = randomMovement.minX;

            if (Global.RANDOM.nextBoolean())
                newMovement.minY = minY;
            else
                newMovement.minY = randomMovement.minY;

            if (Global.RANDOM.nextBoolean())
                newMovement.maxX = maxX;
            else
                newMovement.maxX = randomMovement.maxX;

            if (Global.RANDOM.nextBoolean())
                newMovement.maxY = maxY;
            else
                newMovement.maxY = randomMovement.maxY;

            newMovement.validate();

            if (Global.RANDOM.nextBoolean())
                newMovement.weight = weight;
            else
                newMovement.weight = randomMovement.weight;

            return newMovement;
        }

        return null;
    }

    private void validate() {
        if (minX > maxX) {
            float temp = maxX;
            maxX = minX;
            minX = temp;
        }

        if (minY > maxY) {
            float temp = maxY;
            maxY = minY;
            minY = temp;
        }
    }
}