package com.irlab.view.fragment;

import static com.irlab.view.common.Constants.BOARD_HEIGHT;
import static com.irlab.view.common.Constants.BOARD_WIDTH;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.irlab.view.R;
import com.irlab.view.models.Board;
import com.irlab.view.models.Point;
import com.irlab.view.utils.Drawer;

public class FriendPlayFragment extends Fragment {

    private final Drawer drawer = new Drawer();

    private View view;
    private Board board;
    private ImageView boardImageView;
    private Bitmap boardBitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        board = new Board(19, 19, 0);
        boardBitmap = Bitmap.createBitmap(BOARD_WIDTH, BOARD_HEIGHT, Bitmap.Config.ARGB_8888);
    }

    @Override
    public void onStart() {
        super.onStart();
        drawBoard();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_friend_play, container, false);
        boardImageView = view.findViewById(R.id.iv_board);
        return view;
    }

    private void drawBoard() {
        requireActivity().runOnUiThread(() -> {
            Bitmap board = drawer.drawBoard(boardBitmap, this.board.getBoard(), new Point(-1, -1), 0, 0);
            boardImageView.setImageBitmap(board);
        });
    }
}