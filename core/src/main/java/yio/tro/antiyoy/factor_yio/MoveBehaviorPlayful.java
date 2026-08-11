package yio.tro.antiyoy.factor_yio;

/**
 * Created by yiotro on 24.07.2015.
 */
public class MoveBehaviorPlayful extends MoveBehavior {

    public MoveBehaviorPlayful() {
    }


    @Override
    void move(FactorYio fy) {
        if (fy.itsTimeToStop) return;
        double k = FactorYio.getDtFactor();
        if (fy.f < 1) {
            fy.f += fy.speedMultiplier * fy.dy * k;
            fy.dy += fy.gravity * k;
            if (fy.f > 1) {
                fy.speedMultiplier /= 3;
            }
        } else {
            fy.f += fy.speedMultiplier * fy.dy * k;
            fy.dy -= 3 * fy.gravity * k;
        }
        if (fy.dy < 0 && fy.f < 1) {
            fy.f = 1;
            fy.itsTimeToStop = true;
        }
    }


    @Override
    void alertAboutSpawning(FactorYio fy) {
        super.alertAboutSpawning(fy);
        fy.speedMultiplier *= 2;
        fy.itsTimeToStop = false;
    }


    @Override
    boolean needsToMove(FactorYio fy) {
        return !fy.itsTimeToStop;
    }
}
