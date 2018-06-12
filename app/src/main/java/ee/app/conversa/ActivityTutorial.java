package ee.app.conversa;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ee.app.conversa.extendables.BaseActivity;
import ee.app.conversa.tutorial.Direction;
import ee.app.conversa.tutorial.IndicatorOptions;
import ee.app.conversa.tutorial.PageOptions;
import ee.app.conversa.tutorial.TransformItem;
import ee.app.conversa.tutorial.TutorialFragment;
import ee.app.conversa.tutorial.TutorialOptions;
import ee.app.conversa.tutorial.TutorialPageOptionsProvider;

public class ActivityTutorial extends BaseActivity {

    public static void start(Context context) {
        context.startActivity(new Intent(context, ActivityTutorial.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        if (savedInstanceState == null) {
            replaceTutorialFragment();
        }
    }

    public void replaceTutorialFragment() {
        final IndicatorOptions indicatorOptions = IndicatorOptions.newBuilder(this)
                .setElementColorRes(R.color.green)
                .setSelectedElementColorRes(R.color.green_darker)
                .build();
        final TutorialOptions tutorialOptions = TutorialFragment.newTutorialOptionsBuilder(this)
                .setUseInfiniteScroll(false)
                .setPagesCount(5)
                .setIndicatorOptions(indicatorOptions)
                .setTutorialPageProvider(new TutorialPagesProvider())
                .setOnSkipClickListener(new OnSkipClickListener(this))
                .build();
        final TutorialFragment tutorialFragment = TutorialFragment.newInstance(tutorialOptions);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, tutorialFragment)
                .commit();
    }

    private static final class TutorialPagesProvider implements TutorialPageOptionsProvider {

        @NonNull
        @Override
        public PageOptions provide(int position) {
            @LayoutRes int pageLayoutResId;
            TransformItem[] tutorialItems;
            switch (position) {
                case 0: {
                    pageLayoutResId = R.layout.fragment_tutorial_first;
                    tutorialItems = new TransformItem[]{
                            TransformItem.create(R.id.ivThirdImage, Direction.RIGHT_TO_LEFT, 0.08f)
                    };
                    break;
                }
                case 1: {
                    pageLayoutResId = R.layout.fragment_tutorial_second;
                    tutorialItems = new TransformItem[]{
                            TransformItem.create(R.id.ivThirdImage, Direction.RIGHT_TO_LEFT, 0.08f)
                    };
                    break;
                }
                case 2: {
                    pageLayoutResId = R.layout.fragment_tutorial_third;
                    tutorialItems = new TransformItem[]{
                            TransformItem.create(R.id.ivThirdImage, Direction.RIGHT_TO_LEFT, 0.08f)
                    };
                    break;
                }
                case 3: {
                    pageLayoutResId = R.layout.fragment_tutorial_fourth;
                    tutorialItems = new TransformItem[]{
                            TransformItem.create(R.id.ivThirdImage, Direction.RIGHT_TO_LEFT, 0.08f)
                    };
                    break;
                }
                case 4: {
                    pageLayoutResId = R.layout.fragment_tutorial_fifth;
                    tutorialItems = new TransformItem[]{
                            TransformItem.create(R.id.ivThirdImage, Direction.RIGHT_TO_LEFT, 0.08f)
                    };
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unknown position: " + position);
                }
            }

            return PageOptions.create(pageLayoutResId, position, tutorialItems);
        }
    }

    private static final class OnSkipClickListener implements View.OnClickListener {

        @NonNull
        private final AppCompatActivity mContext;

        OnSkipClickListener(@NonNull AppCompatActivity context) {
            mContext = context;
        }

        @Override
        public void onClick(View v) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                mContext.startActivity(new Intent(mContext, ActivityMain.class));
            } else {
                mContext.startActivity(new Intent(mContext, ActivitySignIn.class));
            }
        }
    }
}
