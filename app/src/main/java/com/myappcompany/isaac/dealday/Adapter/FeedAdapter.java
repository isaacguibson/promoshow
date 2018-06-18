package com.myappcompany.isaac.dealday.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.myappcompany.isaac.dealday.BD.DataBaseService;
import com.myappcompany.isaac.dealday.Constantes.ConstantesPlataformas;
import com.myappcompany.isaac.dealday.Model.Item;
import com.myappcompany.isaac.dealday.Model.RSSObject;
import com.myappcompany.isaac.dealday.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by isaac on 19/03/18.
 */

//Classe Adapter, serve para gerenciar que itens aparecem no RecycleView
public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder>{

    private RSSObject rssObject;
    private Context mContext;
    private LayoutInflater inflater;
    private int viewPosition;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat();

    public FeedAdapter(RSSObject rssObject, Context mContext) {

        this.rssObject = rssObject;
        this.mContext = mContext;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public FeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.row, parent, false);
        return new FeedViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final FeedViewHolder holder, int position) {
        Item item = rssObject.getItems().get(position);
        Date date = item.getPubDate();
        String desc = item.getDescription();

        if(item.isActiveAdMob()){

            holder.getImageView().setPadding(0,0,0,0);
            holder.getTxtTitle().setPadding(0,0,0,0);
            holder.getTxtContent().setPadding(0,0,0,0);
            holder.getImageButton().setPadding(0,0,0,0);
            holder.getShareButton().setPadding(0,0,0,0);
            holder.getFavoriteButton().setPadding(0,0,0,0);

            holder.getImageView().setVisibility(View.GONE);
            holder.getTxtTitle().setVisibility(View.GONE);
            holder.getTxtContent().setVisibility(View.GONE);
            holder.getImageButton().setVisibility(View.GONE);
            holder.getShareButton().setVisibility(View.GONE);
            holder.getFavoriteButton().setVisibility(View.GONE);

            holder.getmAdView().setVisibility(View.VISIBLE);

            //Retirar trecho .addTestDevice("E1CA40D6642FD9AB962B3B6DCBF8331B") quando for pra producao
            AdRequest adRequest = new AdRequest.Builder().build();
            holder.getmAdView().loadAd(adRequest);

            holder.getmAdView().setAdListener(new AdListener(){
                @Override
                public void onAdFailedToLoad(int i) {
                    holder.getmAdView().setVisibility(View.GONE);
                }
            });

            return;
        }

        holder.getmAdView().setVisibility(View.GONE);

        holder.getTxtTitle().setText(item.getTitle());
        if(date != null){
            holder.getTxtPubDate().setText(simpleDateFormat.format(date));
        }
        if(desc != null){
            holder.getTxtContent().setText(item.getDescription());
        }

        if(item.isFavorite()){
            holder.getFavoriteButton().setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_button_pressed));
        }

        //Seting imageView to downloading image
        if(rssObject.getItems().get(position).getUrlImage() == null){

            //TODO criar funcao preencher imagem
            holder.getImageView().setScaleType(ImageView.ScaleType.CENTER);
            fillImages(holder, rssObject.getItems().get(position).getCategory());

        }else{
            new DownloadImageTask(holder.getImageView()).execute(rssObject.getItems().get(position).getUrlImage());
        }

        fillImageLogo(holder, rssObject.getItems().get(position).getPlatform());


    }

    private void fillImages(final FeedViewHolder holder, String category){

        switch (category){
            case "Automotivos":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_automotivo));
                break;
            case "Bebes e Crianças":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_criancas));
                break;
            case "Casa e Cozinha":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_casa));
                break;
            case "Celulares e Smartphones":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_smartphone));
                break;
            case "Coisas e Produtos Grátis":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_gratis));
                break;
            case "Comida e Bebida":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_comida));
                break;
            case "Computadores e Informática":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_informatica));
                break;
            case "Eletrodomésticos":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_eletro));
                break;
            case "Entretenimento e Lazer":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_lazer));
                break;
            case "Esportes e Execícios":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_exercicio));
                break;
            case "Livros":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_livros));
                break;
            case "Moda e Acessórios":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_moda));
                break;
            case "PC, PlayStation e Xbox":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_videogames));
                break;
            case "Presentes e Serviços":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_presente));
                break;
            case "Restaurantes":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_restaurante));
                break;
            case "Saúde e Beleza":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_beleza));
                break;
            case "Tecnologia e Escritório":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_escritorio));
                break;
            case "TV, Som e Vídeo":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_tv));
                break;
            case "Viagens":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_viagem));
                break;
            case "Outros":
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.icone_outros));
                break;
        }
    }

    public void fillImageLogo(FeedViewHolder holder, String platform){
        switch (platform){
            case ConstantesPlataformas.PELANDO:
                holder.getImageButton().setImageDrawable(mContext.getResources().getDrawable(R.drawable.pelando_logo));
                break;
            case ConstantesPlataformas.HARDMOB:
                holder.getImageButton().setImageDrawable(mContext.getResources().getDrawable(R.drawable.hardmob_logo));
                break;
            case ConstantesPlataformas.PROMOBIT:
                holder.getImageButton().setImageDrawable(mContext.getResources().getDrawable(R.drawable.promobit_logo));
                break;
            case ConstantesPlataformas.PROMOFORUM:
                holder.getImageButton().setImageDrawable(mContext.getResources().getDrawable(R.drawable.promoforum_logo));
                break;
            case ConstantesPlataformas.ADRENALINE:
                holder.getImageButton().setImageDrawable(mContext.getResources().getDrawable(R.drawable.adrenaline_logo));
                break;
            case ConstantesPlataformas.MELHORES_DESTINOS:
                holder.getImageButton().setImageDrawable(mContext.getResources().getDrawable(R.drawable.melhores_destinos_logo));
                break;
            case ConstantesPlataformas.PASSAGENS_IMPERDIVEIS:
                holder.getImageButton().setImageDrawable(mContext.getResources().getDrawable(R.drawable.passagens_imperdiveis_logo));
                break;
        }
    }

    public int getViewPosition() {
        return viewPosition;
    }

    public void setViewPosition(int viewPosition) {
        this.viewPosition = viewPosition;
    }

    @Override
    public int getItemCount() {
        return rssObject.getItems().size();
    }

    public RSSObject getRssObject() {
        return rssObject;
    }

    public void setRssObject(RSSObject rssObject) {
        this.rssObject = rssObject;
    }

    class FeedViewHolder extends RecyclerView.ViewHolder {


        private TextView txtTitle, txtPubDate, txtContent;
        private ImageView imageView;
        private ImageButton favoriteButton;
        private ImageButton imageButton;
        private ImageButton shareButton;
        private DataBaseService dataBaseService;

        private AdView mAdView;

        public FeedViewHolder(View itemView) {
            super(itemView);

            txtTitle = (TextView) itemView.findViewById(R.id.txtTitle);
            txtPubDate = (TextView) itemView.findViewById(R.id.txtPubDate);
            txtContent = (TextView) itemView.findViewById(R.id.txtDescription);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            favoriteButton = (ImageButton) itemView.findViewById(R.id.imageFavoriteButton);
            imageButton = (ImageButton) itemView.findViewById(R.id.imageLogoButton);
            shareButton = (ImageButton) itemView.findViewById(R.id.imageShareButton);
            mAdView = (AdView) itemView.findViewById(R.id.itemAdView);

            txtTitle.setOnClickListener(linstener);
            txtPubDate.setOnClickListener(linstener);
            txtContent.setOnClickListener(linstener);
            imageView.setOnClickListener(linstener);
            favoriteButton.setOnClickListener(favoriteListener);
            shareButton.setOnClickListener(shareListener);
        }

        private View.OnClickListener favoriteListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Item item = rssObject.getItems().get(getLayoutPosition());
                dataBaseService = new DataBaseService(mContext);
                if(!item.isFavorite()){
                    item.setFavorite(true);
                    dataBaseService.update(item);
                    favoriteButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_button_pressed));
                } else {
                    item.setFavorite(false);
                    dataBaseService.update(item);
                    favoriteButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_button));
                }

            }
        };

        private View.OnClickListener shareListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item item = rssObject.getItems().get(getLayoutPosition());
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                share.putExtra(Intent.EXTRA_SUBJECT, "Promoshow: "+item.getTitle());
                share.putExtra(Intent.EXTRA_TEXT, item.getLink());

                mContext.startActivity(Intent.createChooser(share, "Compartilhar"));
            }
        };

        private View.OnClickListener linstener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(rssObject.getItems().get(getLayoutPosition()).getLink()));
                v.getContext().startActivity(myIntent);
            }
        };

        public TextView getTxtTitle() {
            return txtTitle;
        }

        public void setTxtTitle(TextView txtTitle) {
            this.txtTitle = txtTitle;
        }

        public TextView getTxtPubDate() {
            return txtPubDate;
        }

        public void setTxtPubDate(TextView txtPubDate) {
            this.txtPubDate = txtPubDate;
        }

        public TextView getTxtContent() {
            return txtContent;
        }

        public void setTxtContent(TextView txtContent) {
            this.txtContent = txtContent;
        }

        public ImageView getImageView() {
            return imageView;
        }

        public void setImageView(ImageView imageView) {
            this.imageView = imageView;
        }

        public ImageButton getFavoriteButton() {
            return favoriteButton;
        }

        public void setFavoriteButton(ImageButton favoriteButton) {
            this.favoriteButton = favoriteButton;
        }

        public ImageButton getShareButton() {
            return shareButton;
        }

        public void setShareButton(ImageButton shareButton) {
            this.shareButton = shareButton;
        }

        public ImageButton getImageButton() {
            return imageButton;
        }

        public void setImageButton(ImageButton imageButton) {
            this.imageButton = imageButton;
        }

        public AdView getmAdView() {
            return mAdView;
        }

        public void setmAdView(AdView mAdView) {
            this.mAdView = mAdView;
        }
    }
}
