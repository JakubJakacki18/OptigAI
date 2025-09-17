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

class LoadingFragment : Fragment() {
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
