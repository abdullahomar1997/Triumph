package com.example.triumph;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameActivityNav3 extends AppCompatActivity {

    String GameId;
    String PlayerId = "Abdullah";

    FirebaseFirestore firestore;

    ActionBarDrawerToggle toggle;

    String playerData = "";
    String gameData = "";
    String gamestatus = "";

    int currentStreak = 0;
    int highestStreak = 0;
    String Triump = "Spades";

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

    private DatabaseHelper databaseHelper;

    private int currentApiVersion;
    private Player player1;
    private Player player2;

    private DrawerLayout drawerLayout;
    NavigationView navigationView;
    private ImageView onlineBtn;
    private Dialog createJoinDialog;
    private Dialog joinGameDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_nav3);

        firestore = FirebaseFirestore.getInstance();

        Game game;

        databaseHelper = new DatabaseHelper(this);

        getGameData(databaseHelper);
        getStreakData(databaseHelper);
        printStreakData();

        if (gamestatus.equals("ongoing")) {

            game = new Game(playerData, gameData);

            player1 = game.players.get(0);
            player2 = game.players.get(1);

            cpuPlayFirst(game, player2);

        } else {
            game = new Game(2, Triump, "easy");
            player1 = game.players.get(0);
            player2 = game.players.get(1);
        }

        setupUserInterface();

        setTriumphCardImage(game);

        updateCards();

        cardButtonA1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Play(game, 0, cardButtonA1);
            }
        });

        cardButtonA2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Play(game, 1, cardButtonA2);
            }
        });

        cardButtonA3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Play(game, 2, cardButtonA3);
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

        onlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAndShowOnlineMenuDialog();
            }
        });
    }

    public void Play(Game game, int cardNumber, ImageView button) {

        player1.playedCard = player1.cards.get(cardNumber);

        if (game.youWin) {

            game.PlayFirst(player1, player2, player1.playedCard);
            button.setVisibility(View.INVISIBLE);

            saveCurrentGameData(game, "ongoing");

            playCard(player1, player1CardPlayed, slideup);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    playCard(player2, player2CardPlayed, slidedown);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            resetPlayedCards(game);

                            cpuPlayFirst(game, player2);
                        }
                    }, 2000);
                }
            }, 1000);
        } else {

            game.PlaySecond(player1, player2, player1.playedCard);
            button.setVisibility(View.INVISIBLE);

            saveCurrentGameData(game, "ongoing");

            playCard(player1, player1CardPlayed, slideup);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    resetPlayedCards(game);

                    cpuPlayFirst(game, player2);
                }
            }, 2000);
        }

        if (game.isGameOver()) {
            //game.triumphCard = game.switchTriumphCard();
            saveCurrentGameData(game, "done");
            displayGameScore(game);
        }
    }

    private void cpuPlayFirst(Game game, Player player2) {
        if (!game.youWin && !game.isGameOver()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    player2.playedCard = game.cpuPlayFirstAutomatically(player2);
                    playCard(player2, player2CardPlayed, slidedown);

                }
            }, 200);
        }
    }

    private void setTriumphCardImage(Game game) {
        ImageView triumphCardImage = (ImageView) findViewById(R.id.triumph);
        triumphCardImage.setImageBitmap(convertStringCardToImage(game.getTriumphCardImage()));
    }

    private void displayGameScore(Game game) {

        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.gameoverlayout, null);
        ListView playerScoreLv = (ListView) view.findViewById(R.id.listview);
        GameOverAdapterDialog clad = new GameOverAdapterDialog(GameActivityNav3.this, game);
        playerScoreLv.setAdapter(clad);

        createGameOverDialog(view);

        TextView outcomeTv = (TextView) scoresDialog.findViewById(R.id.gameoutcometv);
        ImageView newGameIv = (ImageView) scoresDialog.findViewById(R.id.newiv);

        outcomeTv.setText(game.getMatchOutcome());

        newGameIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GameActivityNav3.this, GameActivityNav3.class);
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

    public void updateCards() {

        cardButtonA1.setVisibility(View.INVISIBLE);
        cardButtonA2.setVisibility(View.INVISIBLE);
        cardButtonA3.setVisibility(View.INVISIBLE);

        if (player1.cards.size() > 0) {
            cardButtonA1.setVisibility(View.VISIBLE);
            cardButtonA1.setImageBitmap(convertStringCardToImage(player1.cards.get(0).cardImageCode));
        }

        if (player1.cards.size() > 1) {
            cardButtonA2.setVisibility(View.VISIBLE);
            cardButtonA2.setImageBitmap(convertStringCardToImage(player1.cards.get(1).cardImageCode));
        }

        if (player1.cards.size() > 2) {
            cardButtonA3.setVisibility(View.VISIBLE);
            cardButtonA3.setImageBitmap(convertStringCardToImage(player1.cards.get(2).cardImageCode));
        }
    }

    private void resetPlayedCards(Game game) {

        takeCards(game);

        player1score.setText(String.valueOf(player1.playerScore));
        player2score.setText(String.valueOf(player2.playerScore));

        updateCards();
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

        navigationView = (NavigationView)findViewById(R.id.nav_view);
      //  navigationView.setNavigationItemSelectedListener(this);

        drawerLayout = findViewById(R.id.drawer_layout);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                int id = menuItem.getItemId();

                if(id == R.id.nav_score) {
                    createLeaveDialog();
                    //loadFragment(new galleryFragment());
                } else if(id == R.id.nav_share) {
                    createLeaveDialog();
                    //loadFragment(new Fragment());
                } else if(id == R.id.nav_settings) {
                    createLeaveDialog();
                    //loadFragment(new Fragment());
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

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

        onlineBtn = (ImageView) findViewById(R.id.info);


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

    private void saveCurrentGameData(Game game, String status) {
        databaseHelper.saveGameData(game, status);

        if (status.equals("done")) {
            databaseHelper.saveStreakData(game, highestStreak, currentStreak);
        }
    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void createGameOverDialog(View view) {

        scoresDialog = new Dialog(GameActivityNav3.this);
        scoresDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        scoresDialog.setCancelable(false);
        scoresDialog.setCanceledOnTouchOutside(false);
        scoresDialog.setContentView(view);
        scoresDialog.show();

    }

    public void createLeaveDialog() {

        leaveDialog = new Dialog(GameActivityNav3.this);
        leaveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveDialog.setCancelable(false);
        leaveDialog.setCanceledOnTouchOutside(false);
        leaveDialog.setContentView(R.layout.leavegamelayout);

    }

    private void createAndShowOnlineMenuDialog() {

        createJoinDialog = new Dialog(GameActivityNav3.this);
        createJoinDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //createJoinDialog.setCancelable(false);
        //createJoinDialog.setCanceledOnTouchOutside(false);
        createJoinDialog.setContentView(R.layout.createjoinlayout);

        Button createBtn = (Button)createJoinDialog.findViewById(R.id.onlinecreatebtn);
        Button joinBtn = (Button)createJoinDialog.findViewById(R.id.onlinejoinbtn);

        createJoinDialog.show();

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onlineCreateButton("abc",2);

            }
        });

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openJoinGameDialogMenu();

            }
        });
    }

    private void openJoinGameDialogMenu() {

        joinGameDialog = new Dialog(GameActivityNav3.this);
        joinGameDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //joinGameDialog.setCancelable(false);
        //joinGameDialog.setCanceledOnTouchOutside(false);
        joinGameDialog.setContentView(R.layout.joinlayout);

        joinGameDialog.show();

        EditText gameIdEt = (EditText) joinGameDialog.findViewById(R.id.searchGameEt);
        Button joinBtn = (Button) joinGameDialog.findViewById(R.id.joinbtn);

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GameId = (String)gameIdEt.getText().toString();

                Intent intent = new Intent(GameActivityNav3.this, OnlineGameActivity.class);
                intent.putExtra("gameid", GameId);
                intent.putExtra("playerid", PlayerId);

                startActivity(intent);
            }
        });
    }

    public void createBetDialog() {

        leaveDialog = new Dialog(GameActivityNav3.this);
        leaveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveDialog.setCancelable(false);
        leaveDialog.setCanceledOnTouchOutside(false);
        leaveDialog.setContentView(R.layout.dialog_bet);
    }

    public void createBuyDialog() {

        leaveDialog = new Dialog(GameActivityNav3.this);
        leaveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveDialog.setCancelable(false);
        leaveDialog.setCanceledOnTouchOutside(false);
        leaveDialog.setContentView(R.layout.dialog_buy);
    }

    public void createInstructionsDialog() {

        leaveDialog = new Dialog(GameActivityNav3.this);
        leaveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveDialog.setCancelable(false);
        leaveDialog.setCanceledOnTouchOutside(false);
        leaveDialog.setContentView(R.layout.dialog_instructions);
    }

    public void createSupportDialog() {

        leaveDialog = new Dialog(GameActivityNav3.this);
        leaveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveDialog.setCancelable(false);
        leaveDialog.setCanceledOnTouchOutside(false);
        leaveDialog.setContentView(R.layout.dialog_support);
    }

    public void createShareDialog() {

        leaveDialog = new Dialog(GameActivityNav3.this);
        leaveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveDialog.setCancelable(false);
        leaveDialog.setCanceledOnTouchOutside(false);
        leaveDialog.setContentView(R.layout.dialog_share);
    }

    public void createSettingsDialog() {

        leaveDialog = new Dialog(GameActivityNav3.this);
        leaveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveDialog.setCancelable(false);
        leaveDialog.setCanceledOnTouchOutside(false);
        leaveDialog.setContentView(R.layout.dialog_settings);
    }

    public void createProfileDialog() {

        leaveDialog = new Dialog(GameActivityNav3.this);
        leaveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leaveDialog.setCancelable(false);
        leaveDialog.setCanceledOnTouchOutside(false);
        leaveDialog.setContentView(R.layout.dialog_profile);
    }

    public void createPolicyDialog() {

        leaveDialog = new Dialog(GameActivityNav3.this);
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
            ViewHolder holder;

            if (convertView == null) {

                convertView = layoutInflater.inflate(R.layout.gameoverlistitem, null);
                holder = new ViewHolder();
                holder.playerRankTv = (TextView) convertView.findViewById(R.id.ranktv);
                holder.playernameTv = (TextView) convertView.findViewById(R.id.playernametv);
                holder.playerScoreTv = (TextView) convertView.findViewById(R.id.scoretv);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
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

    private void getGameData(DatabaseHelper databaseHelper) {

        Cursor cursor = databaseHelper.getGameData();

        cursor.moveToLast();

        if (cursor.getCount() > 0) {
            playerData = cursor.getString(1);
            gameData = cursor.getString(2);
            gamestatus = cursor.getString(3);
        }

        cursor.close();
    }

    private void getStreakData(DatabaseHelper databaseHelper) {

        Cursor cursor = databaseHelper.getStreakData();

        cursor.moveToLast();

        if (cursor.getCount() > 0) {

            highestStreak = Integer.parseInt(cursor.getString(1));
            currentStreak = Integer.parseInt(cursor.getString(2));
            Triump = cursor.getString(3);
        }
        cursor.close();
    }

    private void printStreakData() {

        Cursor cursor = databaseHelper.getStreakData();

        //cursor.moveToLast();

        String tt = "Streak is ";

        while (cursor.moveToNext()) {

            highestStreak = Integer.parseInt(cursor.getString(1));
            currentStreak = Integer.parseInt(cursor.getString(2));
            Triump = cursor.getString(3);

            tt += cursor.getString(1) + "/" + cursor.getString(2) + "/" + cursor.getString(3);
            tt += "---";
        }

        //  toastMessage(tt);

        cursor.close();
    }

    public boolean onNavigationItemSelected(MenuItem menuItem) {

        Toast.makeText(this, "Click", Toast.LENGTH_SHORT).show();

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

    public void onlineCreateButton(String playerID, int numPlayers) {

        Toast.makeText(this,"entered",Toast.LENGTH_LONG).show();

        Map<String, Object> gameMap = new HashMap<>();

        gameMap.put("player1card","");
        gameMap.put("player2card","");
        gameMap.put("turn",0);

        firestore.collection("games").add(gameMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

                String gameID = documentReference.getId();
                Log.w("Create Game", "Game Succesfully Created With ID : " + gameID);
                createMultiplePlayerGames(playerID, gameID, numPlayers);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GameActivityNav3.this,"failed",Toast.LENGTH_LONG).show();
                        Log.w("Create Game", "Error adding document", e);
                    }
                });
    }

    private void createMultiplePlayerGames(String playerID, String gameID, int numberOfPlayers) {

        List<Player> decks = Game.getTwoDecks();

        for (int i = 0; i < numberOfPlayers; ++i) {
            createPlayerGame(gameID, i,decks.get(i));
        }

        createOnlineMenuDialog(playerID, gameID);
    }

    public void createOnlineMenuDialog(String playerID, String gameID) {

        Dialog shareInviteDialog = new Dialog(GameActivityNav3.this);
        shareInviteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //numberDialog.setCancelable(false);
        //numberDialog.setCanceledOnTouchOutside(false);
        shareInviteDialog.setContentView(R.layout.createlayout);

        ImageView startGameBtn = (ImageView) shareInviteDialog.findViewById(R.id.btnNumber);
        Button shareGameBtn = (Button) shareInviteDialog.findViewById(R.id.btnShare);
        TextView tableCodeTv = (TextView) shareInviteDialog.findViewById(R.id.tableCode);

        shareInviteDialog.show();

        tableCodeTv.setText(gameID);

        shareGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_SEND);
                intent1.putExtra(Intent.EXTRA_TEXT, gameID);

                intent1.setType("text/plain");
                intent1.setPackage("com.whatsapp");

                startActivity(intent1);
            }
        });

        startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(GameActivityNav3.this, OnlineGameActivity.class);
                intent.putExtra("gameid", gameID);
                intent.putExtra("playerid", playerID);

                startActivity(intent);

            }
        });
    }


    public void createPlayerGame(String gameID, int position,Player player) {

        Map<String, Object> playerGameMap = new HashMap<>();

        String deck = player.cards.toString().replaceAll("\\[", "").replaceAll("\\]","");

        playerGameMap.put("playerID", "");
        playerGameMap.put("gameID", gameID);
        playerGameMap.put("position", position);
        playerGameMap.put("deck", deck);

        firestore.collection("playerGames").add(playerGameMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("Create Player", "Player Created with ID: " + documentReference.getId());
                        //String pg = documentReference.getId();
                        //Toast.makeText(OnlineGameMenu.this, "Player is " + pg , Toast.LENGTH_SHORT).show();
                        //ids.add(pg);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Create Player", "Error Creating Player", e);
                    }
                });
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
}