package br.gov.dpf.intelitrack;

import android.os.Bundle;
import android.view.View;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

/**
 * Created by André Costa on 27/04/2018.
 */

public class FirstStartActivity extends IntroActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Fullscreen
        setFullscreen(true);

        //Base onCreate method
        super.onCreate(savedInstanceState);

        //Define skip intro click behavior
        View.OnClickListener skipIntro = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };

        //Fist slide - Welcome
        addSlide(new SimpleSlide.Builder()
                .titleHtml("Bem vindo ao <b><u>InteliTrack</u></b>")
                .description("Plataforma de rastreamento de ativos desenvolvido no âmbito da Delegacia de Polícia Federal de Naviraí/MS.")
                .image(R.drawable.intro_map)
                .background(R.color.page_welcome)
                .backgroundDark(R.color.page_welcome_dark)
                .scrollable(true)
                .buttonCtaLabel("Pular introdução")
                .buttonCtaClickListener(skipIntro)
                .build());

        //Second slide - Goals
        addSlide(new SimpleSlide.Builder()
                .titleHtml("Objetivo da plataforma")
                .description("A principal finalidade deste sistema é permitir, de forma simples e padronizada, o gerenciamento de diversos modelos de dispositivos rastreadores.")
                .image(R.drawable.intro_goal)
                .background(R.color.page_goals)
                .backgroundDark(R.color.page_goals_dark)
                .scrollable(true)
                .buttonCtaLabel("Pular introdução")
                .buttonCtaClickListener(skipIntro)
                .build());

        //Third slide - Features
        addSlide(new SimpleSlide.Builder()
                .titleHtml("Principais funcionalidades")
                .description("Notificações em tempo real, histórico de localizações, configurações personalizadas para cada modelo de rastreador, entre outras a serem adicionadas.")
                .image(R.drawable.intro_features)
                .background(R.color.page_features)
                .backgroundDark(R.color.page_features_dark)
                .scrollable(true)
                .buttonCtaLabel("Pular introdução")
                .buttonCtaClickListener(skipIntro)
                .build());

        //Fourth slide - Development
        addSlide(new SimpleSlide.Builder()
                .titleHtml("Desenvolvimento contínuo")
                .description("Por se tratar de uma plataforma em desenvolvimento, sugestões de melhoria serão bem vindas aos administradores do sistema.")
                .image(R.drawable.intro_development)
                .background(R.color.page_development)
                .backgroundDark(R.color.page_development_dark)
                .scrollable(true)
                .buttonCtaLabel("Acessar sistema")
                .buttonCtaClickListener(skipIntro)
                .build());
    }
}
