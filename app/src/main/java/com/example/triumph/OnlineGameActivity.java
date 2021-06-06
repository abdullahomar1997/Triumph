package com.example.triumph;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnlineGameActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    int position;

    String Triump = "t s";

    private DrawerLayout drawerLayout;

    private TextView player1score;
    private TextView player2score;

    private Animation player1win;
    private Animation player2win;
    private Animation slideup;
    private Animation slidedown;

    private ImageView player1CardPlayed;
    private ImageView player2CardPlayed;

    private ImageView cardButtonA1;
    private ImageView cardButtonA2;
    private ImageView cardButtonA3;

    private ImageView leaveGameBtn;

    private Dialog leaveDialog;
    private Dialog scoresDialog;

    private int currentApiVersion;

    //NavigationView navigationView;
    private String GameId;
    private String PlayerId;

    FirebaseFirestore firestore;
    private boolean myTurn;

    int myScore = 0;
    int cpuScore = 0;

    List<Card> myDeck = new ArrayList<>();

    private String player1card;
    private String player2card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game);

        firestore = FirebaseFirestore.getInstance();

        Intent bundle = getIntent();
        GameId = bundle.getStringExtra("gameid");
        PlayerId = bundle.getStringExtra("playerid");

        joinPlayerToGame(GameId, PlayerId);

        setupUserInterface();

        setTriumphCardImage(Triump);

        updateCards(myDeck);

        cardButtonA1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Play(0, cardButtonA1);
            }
        });

        cardButtonA2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Play(1, cardButtonA2);
            }
        });

        cardButtonA3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Play(2, cardButtonA3);
            }
        });

        leaveGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                createLeaveDialog();

                ImageView noLeaveBtn = (ImageView) leaveDialog.findViewById(R.id.btnNoDelete);
                ImageView yesLeaveBtn = (ImageView) leaveDialog.findViewById(R.id.btnYesDelete);

                leaveDialog.show();

                noLeaveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        leaveDialog.dismiss();
                    }
                });

                yesLeaveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
            }
        });
    }

    public void Play(int cardNumber, ImageView button) {

        if (myTurn) {

            Card playedCard = myDeck.get(cardNumber);

            myDeck.remove(cardNumber);

            button.setVisibility(View.INVISIBLE);

            myTurn = false;

            playCardOnline(playedCard.cardName);
        }
    }

    private void setTriumphCardImage(String triumph) {
        ImageView triumphCardImage = (ImageView) findViewById(R.id.triumph);
        triumphCardImage.setImageBitmap(convertStringCardToImage("t s"));
    }

    private void displayGameScore(Game game) {

        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.gameoverlayout, null);
        ListView playerScoreLv = (ListView) view.findViewById(R.id.listview);
        GameActivityNav3.GameOverAdapterDialog clad = new GameActivityNav3.GameOverAdapterDialog(OnlineGameActivity.this, game);
        playerScoreLv.setAdapter(clad);

        createGameOverDialog(view);

        TextView outcomeTv = (TextView) scoresDialog.findViewById(R.id.gameoutcometv);
        ImageView newGameIv = (ImageView) scoresDialog.findViewById(R.id.newiv);

        outcomeTv.setText(game.getMatchOutcome());

        newGameIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OnlineGameActivity.this, GameActivityNav3.class);
                startActivity(intent);
            }
        });
    }

    public void youTakeTheCards() {

        player1CardPlayed.startAnimation(player1win);
        player2CardPlayed.startAnimation(player1win);

        player1CardPlayed.setVisibility(View.INVISIBLE);
        player2CardPlayed.setVisibility(View.INVISIBLE);
    }

    public void cpuTakeTheCards() {

        player1CardPlayed.startAnimation(player2win);
        player2CardPlayed.startAnimation(player2win);

        player1CardPlayed.setVisibility(View.INVISIBLE);
        player2CardPlayed.setVisibility(View.INVISIBLE);
    }

    private void takeCards(Game game) {

        if (game.youWin) {
            youTakeTheCards();
        } else {
            cpuTakeTheCards();
        }
    }

    public void updateCards(List<Card> myDeck) {

        cardButtonA1.setVisibility(View.INVISIBLE);
        cardButtonA2.setVisibility(View.INVISIBLE);
        cardButtonA3.setVisibility(View.INVISIBLE);

        if (myDeck.size() > 0) {
            cardButtonA1.setVisibility(View.VISIBLE);
            cardButtonA1.setImageBitmap(convertStringCardToImage(myDeck.get(0).cardImageCode));
        }

        if (myDeck.size() > 1) {
            cardButtonA2.setVisibility(View.VISIBLE);
            cardButtonA2.setImageBitmap(convertStringCardToImage(myDeck.get(1).cardImageCode));
        }

        if (myDeck.size() > 2) {
            cardButtonA3.setVisibility(View.VISIBLE);
            cardButtonA3.setImageBitmap(convertStringCardToImage(myDeck.get(2).cardImageCode));
        }
    }

    private void resetPlayedCards(Game game, int player1Score, int player2Score) {

        takeCards(game);

        player1score.setText(String.valueOf(player1Score));
        player2score.setText(String.valueOf(player2Score));

        updateCards(myDeck);
    }

    private void playCard(Player player, ImageView playerCardPlayed, Animation slide) {
        playerCardPlayed.setVisibility(View.VISIBLE);
        playerCardPlayed.setImageBitmap(convertStringCardToImage(player.playedCard.cardImageCode));
        playerCardPlayed.startAnimation(slide);
    }

    public Bitmap convertStringCardToImage(String Card) {

        Bitmap cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.h7);

        String[] card = Card.split(" ");
        String cardNo = card[0];
        String cardSign = card[1];

        if (cardNo.equals("10") && cardSign.equals("h")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.h10);
        } else if (cardNo.equals("9") && cardSign.equals("h")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.h9);
        } else if (cardNo.equals("8") && cardSign.equals("h")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.h8);
        } else if (cardNo.equals("7") && cardSign.equals("h")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.h7);
        } else if (cardNo.equals("6") && cardSign.equals("h")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.h6);
        } else if (cardNo.equals("5") && cardSign.equals("h")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.h5);
        } else if (cardNo.equals("4") && cardSign.equals("h")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.h4);
        } else if (cardNo.equals("3") && cardSign.equals("h")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.h3);
        } else if (cardNo.equals("2") && cardSign.equals("h")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.h2);
        } else if (cardNo.equals("10") && cardSign.equals("s")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.s10);
        } else if (cardNo.equals("9") && cardSign.equals("s")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.s9);
        } else if (cardNo.equals("8") && cardSign.equals("s")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.s8);
        } else if (cardNo.equals("7") && cardSign.equals("s")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.s7);
        } else if (cardNo.equals("6") && cardSign.equals("s")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.s6);
        } else if (cardNo.equals("5") && cardSign.equals("s")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.s5);
        } else if (cardNo.equals("4") && cardSign.equals("s")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.s4);
        } else if (cardNo.equals("3") && cardSign.equals("s")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.s3);
        } else if (cardNo.equals("2") && cardSign.equals("s")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.s2);
        } else if (cardNo.equals("10") && cardSign.equals("f")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.f10);
        } else if (cardNo.equals("9") && cardSign.equals("f")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.f9);
        } else if (cardNo.equals("8") && cardSign.equals("f")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.f8);
        } else if (cardNo.equals("7") && cardSign.equals("f")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.f7);
        } else if (cardNo.equals("6") && cardSign.equals("f")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.f6);
        } else if (cardNo.equals("5") && cardSign.equals("f")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.f5);
        } else if (cardNo.equals("4") && cardSign.equals("f")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.f4);
        } else if (cardNo.equals("3") && cardSign.equals("f")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.f3);
        } else if (cardNo.equals("2") && cardSign.equals("f")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.f2);
        } else if (cardNo.equals("10") && cardSign.equals("d")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.d10);
        } else if (cardNo.equals("9") && cardSign.equals("d")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.d9);
        } else if (cardNo.equals("8") && cardSign.equals("d")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.d8);
        } else if (cardNo.equals("7") && cardSign.equals("d")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.d7);
        } else if (cardNo.equals("6") && cardSign.equals("d")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.d6);
        } else if (cardNo.equals("5") && cardSign.equals("d")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.d5);
        } else if (cardNo.equals("4") && cardSign.equals("d")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.d4);
        } else if (cardNo.equals("3") && cardSign.equals("d")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.d3);
        } else if (cardNo.equals("2") && cardSign.equals("d")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.d2);
        } else if (cardNo.equals("o") && cardSign.equals("o")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.emptycardback);
        } else if (cardNo.equals("a") && cardSign.equals("d")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.da);
        } else if (cardNo.equals("k") && cardSign.equals("d")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.dk);
        } else if (cardNo.equals("q") && cardSign.equals("d")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.dq);
        } else if (cardNo.equals("j") && cardSign.equals("d")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.dj);
        } else if (cardNo.equals("a") && cardSign.equals("s")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.sa);
        } else if (cardNo.equals("k") && cardSign.equals("s")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.sk);
        } else if (cardNo.equals("q") && cardSign.equals("s")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.sq);
        } else if (cardNo.equals("j") && cardSign.equals("s")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.sj);
        } else if (cardNo.equals("a") && cardSign.equals("h")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.ha);
        } else if (cardNo.equals("k") && cardSign.equals("h")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.hk);
        } else if (cardNo.equals("q") && cardSign.equals("h")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.hq);
        } else if (cardNo.equals("j") && cardSign.equals("h")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.hj);
        } else if (cardNo.equals("a") && cardSign.equals("f")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.fa);
        } else if (cardNo.equals("k") && cardSign.equals("f")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.fk);
        } else if (cardNo.equals("q") && cardSign.equals("f")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.fq);
        } else if (cardNo.equals("j") && cardSign.equals("f")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.fj);
        } else if (cardNo.equals("v") && cardSign.equals("ks")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.kings);
        } else if (cardNo.equals("v") && cardSign.equals("kh")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.kingd);
        } else if (cardNo.equals("v") && cardSign.equals("qf")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.queens);
        } else if (cardNo.equals("v") && cardSign.equals("qd")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.queend);
        } else if (cardNo.equals("v") && cardSign.equals("js")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.jackf);
        } else if (cardNo.equals("v") && cardSign.equals("jh")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.jackd);
        } else if (cardNo.equals("t") && cardSign.equals("s")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.spades);
        } else if (cardNo.equals("t") && cardSign.equals("h")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.hearts);
        } else if (cardNo.equals("t") && cardSign.equals("f")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.flowers);
        } else if (cardNo.equals("t") && cardSign.equals("d")) {
            cardImage = BitmapFactory.decodeResource(getResources(), R.drawable.diamonds);
        }
        return cardImage;
    }

    public void setupUserInterface() {

        //drawerLayout = findViewById(R.id.drawer_layout);
        //navigationView = findViewById(R.id.nav_view);
        //navigationView.setNavigationItemSelectedListener(this);

        //ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        //drawerLayout.addDrawerListener(toggle);

        //toggle.syncState();

        slidedown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide2);
        slideup = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide);

        player1win = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.player1win);
        player2win = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.player2win);

        player1CardPlayed = (ImageView) findViewById(R.id.player1cardplayed);
        player2CardPlayed = (ImageView) findViewById(R.id.player2cardplayed);

        cardButtonA1 = (ImageView) findViewById(R.id.card1);
        cardButtonA2 = (ImageView) findViewById(R.id.card2);
        cardButtonA3 = (ImageView) findViewById(R.id.card3);

        leaveGameBtn = (ImageView) findViewById(R.id.cardoptions);

        currentApiVersion = android.os.Build.VERSION.SDK_INT;
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(flags);
            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(flags);
                    }
                }
            });
        }

        player1score = (TextView) findViewById(R.id.player1score);
        player2score = (TextView) findViewById(R.id.player2score);
    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void createGameOverDialog(View view) {

        scoresDialog = new Dialog(OnlineGameActivity.this);
        scoresDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        scoresDialog.setCancelable(false);
        scoresDialog.setCanceledOnTouchOutside(false);
        scoresDialog.setContentView(view);
        scoresDialog.show();

    }

    public void createLeaveDialog() {

        leaveDialog = new Dialog(OnlineGameActivity.this);
        leaveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveDialog.setCancelable(false);
        leaveDialog.setCanceledOnTouchOutside(false);
        leaveDialog.setContentView(R.layout.leavegamelayout);

    }

    public void createBetDialog() {

        leaveDialog = new Dialog(OnlineGameActivity.this);
        leaveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveDialog.setCancelable(false);
        leaveDialog.setCanceledOnTouchOutside(false);
        leaveDialog.setContentView(R.layout.dialog_bet);
    }

    public void createBuyDialog() {

        leaveDialog = new Dialog(OnlineGameActivity.this);
        leaveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveDialog.setCancelable(false);
        leaveDialog.setCanceledOnTouchOutside(false);
        leaveDialog.setContentView(R.layout.dialog_buy);
    }

    public void createInstructionsDialog() {

        leaveDialog = new Dialog(OnlineGameActivity.this);
        leaveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveDialog.setCancelable(false);
        leaveDialog.setCanceledOnTouchOutside(false);
        leaveDialog.setContentView(R.layout.dialog_instructions);
    }

    public void createSupportDialog() {

        leaveDialog = new Dialog(OnlineGameActivity.this);
        leaveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveDialog.setCancelable(false);
        leaveDialog.setCanceledOnTouchOutside(false);
        leaveDialog.setContentView(R.layout.dialog_support);
    }

    public void createShareDialog() {

        leaveDialog = new Dialog(OnlineGameActivity.this);
        leaveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveDialog.setCancelable(false);
        leaveDialog.setCanceledOnTouchOutside(false);
        leaveDialog.setContentView(R.layout.dialog_share);
    }

    public void createSettingsDialog() {

        leaveDialog = new Dialog(OnlineGameActivity.this);
        leaveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveDialog.setCancelable(false);
        leaveDialog.setCanceledOnTouchOutside(false);
        leaveDialog.setContentView(R.layout.dialog_settings);
    }

    public void createProfileDialog() {

        leaveDialog = new Dialog(OnlineGameActivity.this);
        leaveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveDialog.setCancelable(false);
        leaveDialog.setCanceledOnTouchOutside(false);
        leaveDialog.setContentView(R.layout.dialog_profile);
    }

    public void createPolicyDialog() {

        leaveDialog = new Dialog(OnlineGameActivity.this);
        leaveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveDialog.setCancelable(false);
        leaveDialog.setCanceledOnTouchOutside(false);
        leaveDialog.setContentView(R.layout.dialog_policy);
    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public static class GameOverAdapterDialog extends BaseAdapter {

        private final Game game;

        private final LayoutInflater layoutInflater;

        public GameOverAdapterDialog(Context context, Game game) {
            this.game = game;
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return game.numPlayers;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        public View getView(int position, View convertView, ViewGroup parent) {
            GameActivityNav3.GameOverAdapterDialog.ViewHolder holder;

            if (convertView == null) {

                convertView = layoutInflater.inflate(R.layout.gameoverlistitem, null);
                holder = new GameActivityNav3.GameOverAdapterDialog.ViewHolder();
                holder.playerRankTv = (TextView) convertView.findViewById(R.id.ranktv);
                holder.playernameTv = (TextView) convertView.findViewById(R.id.playernametv);
                holder.playerScoreTv = (TextView) convertView.findViewById(R.id.scoretv);

                convertView.setTag(holder);
            } else {
                holder = (GameActivityNav3.GameOverAdapterDialog.ViewHolder) convertView.getTag();
            }

            String pos = String.valueOf(position + 1);
            String name = String.valueOf(game.players.get(position).playerUsername);
            String score = String.valueOf(game.players.get(position).playerScore);

            holder.playerRankTv.setText(pos);
            holder.playernameTv.setText(name);
            holder.playerScoreTv.setText(score);

            return convertView;
        }

        static class ViewHolder {
            TextView playerRankTv;
            TextView playernameTv;
            TextView playerScoreTv;
        }
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.nav_score:
                Toast.makeText(this, "Score", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_instructions:
                createInstructionsDialog();
                Toast.makeText(this, "Instructions", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_share:
                createShareDialog();
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_rate_us:
                Toast.makeText(this, "Rate", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_support:
                createSupportDialog();
                Toast.makeText(this, "Support", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_privacy_policy:
                createPolicyDialog();
                Toast.makeText(this, "Privacy Policy", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                createSettingsDialog();
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_exit:
                createLeaveDialog();
                Toast.makeText(this, "Exit", Toast.LENGTH_SHORT).show();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void joinPlayerToGame(String gameID, String playerId) {

        CollectionReference playergameRef = firestore.collection("playerGames");

        playergameRef.whereEqualTo("gameID", gameID).whereEqualTo("playerID", "").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        String deck = document.getString("deck");
                        position = document.getLong("position").intValue();

                        myDeck = Game.converstStringToList(deck);

                        updateCards(myDeck);

                        //Toast.makeText(OnlineGameActivity.this, "huhuhu", Toast.LENGTH_SHORT).show();

                        //Toast.makeText(OnlineGameActivity.this, myDeck.toString(), Toast.LENGTH_SHORT).show();

                        if (position == 0) {
                            myTurn = true;
                        }

                        String pgid = document.getId();

                        updatePlayerGame(pgid, playerId);

                        break;
                    }
                }
            }
        });
    }

    private void updatePlayerGame(String pgid, String playerId) {

        DocumentReference pgr = firestore.collection("playerGames").document(pgid);

        pgr.update("playerID", playerId).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                real();
            }
        });
    }

    public void real() {

        DocumentReference documentReference = firestore.collection("games").document(GameId);

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (value != null && value.exists()) {

                    player1card = value.getString("player1card");
                    player2card = value.getString("player2card");
                    int turn = value.getLong("turn").intValue();

                    Card player1Card = Game.getCard(player1card);
                    Card player2Card = Game.getCard(player2card);

                    if (position == 0) {

                        if (!player1card.equals("") && player2card.equals("")) {

                            player1CardPlayed.setVisibility(View.VISIBLE);
                            player1CardPlayed.setImageBitmap(convertStringCardToImage(player1Card.cardImageCode));
                            player1CardPlayed.startAnimation(slideup);

                            myTurn = false;

                        } else if (player1card.equals("") && !player2card.equals("")) {

                            player2CardPlayed.setVisibility(View.VISIBLE);
                            player2CardPlayed.setImageBitmap(convertStringCardToImage(player2Card.cardImageCode));
                            player2CardPlayed.startAnimation(slidedown);

                            myTurn = true;

                        } else if (!player1card.equals("") && !player2card.equals("") && turn == 1) {

                            player2CardPlayed.setVisibility(View.VISIBLE);
                            player2CardPlayed.setImageBitmap(convertStringCardToImage(player2Card.cardImageCode));
                            player2CardPlayed.startAnimation(slidedown);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    if (processWinner(player1Card, player2Card, 1)) {
                                        youTakeTheCards();
                                        myTurn = true;
                                    } else {
                                        cpuTakeTheCards();
                                        myTurn = false;
                                    }

                                    player1score.setText(String.valueOf(myScore));
                                    player2score.setText(String.valueOf(cpuScore));

                                    updateCards(myDeck);

                                    resetCardsOnline();
                                }
                            }, 2000);

                        } else if (!player1card.equals("") && !player2card.equals("") && turn == 0) {

                            player1CardPlayed.setVisibility(View.VISIBLE);
                            player1CardPlayed.setImageBitmap(convertStringCardToImage(player1Card.cardImageCode));
                            player1CardPlayed.startAnimation(slideup);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    if (processWinner(player1Card, player2Card, 2)) {
                                        youTakeTheCards();
                                        myTurn = true;
                                    } else {
                                        cpuTakeTheCards();
                                        myTurn = false;
                                    }

                                    player1score.setText(String.valueOf(myScore));
                                    player2score.setText(String.valueOf(cpuScore));

                                    updateCards(myDeck);

                                    resetCardsOnline();
                                }
                            }, 2000);
                        }
                    }

                    if (position == 1) {

                        if (!player1card.equals("") && player2card.equals("")) {

                            player2CardPlayed.setVisibility(View.VISIBLE);
                            player2CardPlayed.setImageBitmap(convertStringCardToImage(player1Card.cardImageCode));
                            player2CardPlayed.startAnimation(slidedown);

                            myTurn = true;
                        } else if (player1card.equals("") && !player2card.equals("")) {

                            player1CardPlayed.setVisibility(View.VISIBLE);
                            player1CardPlayed.setImageBitmap(convertStringCardToImage(player2Card.cardImageCode));
                            player1CardPlayed.startAnimation(slideup);

                            myTurn = false;
                        } else if (!player1card.equals("") && !player2card.equals("") && turn == 0) {

                            player2CardPlayed.setVisibility(View.VISIBLE);
                            player2CardPlayed.setImageBitmap(convertStringCardToImage(player1Card.cardImageCode));
                            player2CardPlayed.startAnimation(slidedown);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    if (processWinner(player2Card, player1Card, 1)) {
                                        youTakeTheCards();
                                        myTurn = true;
                                    } else {
                                        cpuTakeTheCards();
                                        myTurn = false;
                                    }

                                    player1score.setText(String.valueOf(myScore));
                                    player2score.setText(String.valueOf(cpuScore));

                                    updateCards(myDeck);

                                    resetCardsOnline();
                                }
                            }, 2000);

                        } else if (!player1card.equals("") && !player2card.equals("") && turn == 1) {

                            player1CardPlayed.setVisibility(View.VISIBLE);
                            player1CardPlayed.setImageBitmap(convertStringCardToImage(player2Card.cardImageCode));
                            player1CardPlayed.startAnimation(slideup);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    if (processWinner(player2Card, player1Card, 2)) {
                                        youTakeTheCards();
                                        myTurn = true;
                                    } else {
                                        cpuTakeTheCards();
                                        myTurn = false;
                                    }

                                    player1score.setText(String.valueOf(myScore));
                                    player2score.setText(String.valueOf(cpuScore));

                                    updateCards(myDeck);

                                    resetCardsOnline();
                                }
                            }, 2000);
                        }
                    }
                }
            }
        });
    }

    private boolean processWinner(Card myCard, Card cpuCard, int turn) {

        boolean IWin = false;

        int cardValues = myCard.cardValue + cpuCard.cardValue;

        if (Game.determineWinnerOnline(myCard, cpuCard, turn)) {
            IWin = true;
            myScore += cardValues;
        } else {
            cpuScore += cardValues;
        }
        return IWin;
    }


    //    Show or hide Items in navigation drawer
//        menu = navigationView.getMenu();
//menu.findItem(R.id.nav_logout).setVisible(false);
//        menu.findItem(R.id.nav_profile).setVisible(false);


//    Button hamMenu = findViewById(R.id.ham_menu);
//
//hamMenu.setOnClickListener(new View.OnClickListener() {
//@Override
//public void onClick(View v) {
//        DrawerLayout navDrawer = findViewById(R.id.drawer_layout);
//        // If the navigation drawer is not open then open it, if its already open then close it.
//        if(!navDrawer.isDrawerOpen(Gravity.START)) navDrawer.openDrawer(Gravity.START);
//        else navDrawer.closeDrawer(Gravity.END);
//        }
//        });

    private boolean isAppInstalled(String s) {

        PackageManager pm = getPackageManager();

        boolean isInstalled = true;

        try {
            pm.getPackageInfo("", PackageManager.GET_ACTIVITIES);
            isInstalled = true;

        } catch (PackageManager.NameNotFoundException e) {
            isInstalled = false;
            e.printStackTrace();
        }
        return isInstalled;
    }

    private void playCardOnline(String card) {

        DocumentReference pgr = firestore.collection("games").document(GameId);

        if (position == 0) {

            player1card = card;

            pgr.update("player1card", player1card, "player2card", player2card,"turn",position).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            });
        } else if (position == 1) {

            player2card = card;

            pgr.update("player1card", player1card, "player2card", player2card,"turn",position).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            });
        }
    }

    public void resetCardsOnline() {

        DocumentReference pgr = firestore.collection("games").document(GameId);

        if (position == 0) {

            pgr.update("player1card", "", "player2card", "","turn",position).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    player1card = "";
                    player2card = "";
                }
            });
        }
    }
}


//game:gamedeck,gameturn,gamewinner
//Player : playerId,plyayerscore,gameid,position,cards,playerdeck
//playerGame : player1cardplayed player2cardplayed player1score player2score gameturn gamewinner