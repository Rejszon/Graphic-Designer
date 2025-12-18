package com.sample;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class GuiInterface extends JFrame {
    private DroolsController drools;
    private JPanel contentPanel;
    
    private final Color BG_COLOR = new Color(33, 37, 41);
    private final Color CARD_COLOR = new Color(44, 48, 52);
    private final Color ACCENT_COLOR = new Color(13, 110, 253);

    public GuiInterface() {
        drools = new DroolsController();
        drools.fireRules();

        setTitle("Graphic Design Career Advisor");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_COLOR);
        
        setLayout(new BorderLayout());

        contentPanel = new JPanel();
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setLayout(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        refreshView();
    }

    private void refreshView() {
        contentPanel.removeAll();
        
        Object reaction = drools.getCurrentReaction();
        if (reaction != null) {
            renderReaction(reaction);
            updateUI();
            return;
        }
        
        List<Object> recs = drools.getRecommendations();
        if (!recs.isEmpty()) {
            renderResult(recs.get(0));
            updateUI();
            return;
        }

        Object question = drools.getCurrentQuestion();
        if (question != null) {
            renderQuestion(question);
        } else {
            renderError("Błąd: Silnik zakończył pracę, ale brak wyniku.");
        }
        updateUI();
    }
    
    private void updateUI() {
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void renderQuestion(Object qFact) {
        Object qEnum = drools.getFieldValue(qFact, "content");
        String text = drools.getContentFromEnum(qEnum);

        JPanel card = createCard();
        
        JLabel label = new JLabel("<html><center>"+text+"</center></html>");
        label.setFont(new Font("Segoe UI", Font.BOLD, 28));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(label);
        card.add(Box.createVerticalStrut(40));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);
        
        List<String> options = drools.getAnswerOptions();
        for (String opt : options) {
            Color btnColor = opt.equalsIgnoreCase("YES") ? new Color(25, 135, 84) : new Color(220, 53, 69);
            if (!opt.equalsIgnoreCase("YES") && !opt.equalsIgnoreCase("NO")) {
                 btnColor = Color.GRAY; 
            }
            JButton btn = createButton(opt, btnColor);
            btn.addActionListener(e -> submitAnswer(qFact, qEnum, opt));
            btnPanel.add(btn);
        }
        
        card.add(btnPanel);
        contentPanel.add(card);
    }

    private void renderReaction(Object rFact) {
        Object rEnum = drools.getFieldValue(rFact, "content");
        String text = drools.getContentFromEnum(rEnum);

        JPanel card = createCard();
        
        JLabel header = new JLabel("Analysis");
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setForeground(Color.GRAY);
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(header);
        card.add(Box.createVerticalStrut(10));

        JLabel label = new JLabel("<html><center style='width:600px'>"+text+"</center></html>");
        label.setFont(new Font("Segoe UI", Font.ITALIC, 22));
        label.setForeground(new Color(200, 200, 200));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(label);
        card.add(Box.createVerticalStrut(40));

        JButton btnContinue = createButton("Continue >", ACCENT_COLOR);
        btnContinue.addActionListener(e -> {
            drools.retractObject(rFact);
            drools.fireRules();
            refreshView();
        });
        btnContinue.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(btnContinue);
        contentPanel.add(card);
    }

    private void renderResult(Object recFact) {
        Object resEnum = drools.getFieldValue(recFact, "content");
        String raw = drools.getContentFromEnum(resEnum);
        String[] parts = raw.split("\\|"); 

        JPanel card = createCard();
        card.setBorder(BorderFactory.createLineBorder(Color.GREEN, 4));

        JLabel titleLabel = new JLabel("VERDICT");
        titleLabel.setForeground(Color.GREEN);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(30));

        if(parts.length >= 1) {
            JLabel name = new JLabel("<html><center style='width:800px'>" + parts[0] + "</center></html>");
            name.setFont(new Font("Segoe UI", Font.BOLD, 48));
            name.setForeground(Color.WHITE);
            name.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(name);
        }
        
        if(parts.length >= 2) {
            card.add(Box.createVerticalStrut(30));
            JTextArea descArea = new JTextArea(parts[1]);
            descArea.setFont(new Font("Segoe UI", Font.PLAIN, 24));
            descArea.setForeground(Color.LIGHT_GRAY);
            descArea.setOpaque(false);
            descArea.setEditable(false);
            descArea.setLineWrap(true);
            descArea.setWrapStyleWord(true);
            
            JPanel textWrapper = new JPanel(new BorderLayout());
            textWrapper.setOpaque(false);
            textWrapper.add(descArea, BorderLayout.CENTER);
            textWrapper.setMaximumSize(new Dimension(900, 10000)); 
            
            card.add(textWrapper);
        }
        
        contentPanel.add(card);
    }
    
    private void submitAnswer(Object qFact, Object qEnum, String answer) {
        drools.insertAnswer(qEnum, answer);
        drools.retractObject(qFact);
        drools.fireRules();
        refreshView();
    }

    private JPanel createCard() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD_COLOR);
        p.setBorder(new EmptyBorder(60, 80, 60, 80));
        return p;
    }

    private JButton createButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(140, 50));
        return b;
    }
    
    private void renderError(String msg) {
        JLabel l = new JLabel(msg);
        l.setForeground(Color.RED);
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        contentPanel.add(l);
    }
}