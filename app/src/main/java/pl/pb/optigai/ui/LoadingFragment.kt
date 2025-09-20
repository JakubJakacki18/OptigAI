package pl.pb.optigai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.compose.ui.platform.ComposeView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.core.content.ContextCompat
import pl.pb.optigai.R

/**
 * A Fragment that displays a loading screen with a custom animation.
 * This fragment uses Jetpack Compose to render its UI.
 */
class LoadingFragment : Fragment() {
    /**
     * Called to create the view hierarchy associated with the fragment.
     * It sets up a ComposeView to display a full-screen loading animation.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val composeView = ComposeView(requireContext())
        composeView.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(ContextCompat.getColor(requireContext(), R.color.light_blue))),
                contentAlignment = Alignment.Center
            ) {
                LoadingAnimation(
                    circleColor = Color(ContextCompat.getColor(requireContext(), R.color.creme))
                )
            }
        }
        return composeView
    }
}