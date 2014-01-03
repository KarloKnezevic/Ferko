package hr.fer.zemris.jcms.model.questions;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Pitanje otvorenog tipa koje se ručno ispravlja, smješteno unutar grupe
 * pitanja.
 * 
 * @author Alan Sambol
 */
@Entity
@DiscriminatorValue("O")
public class OpenEndedQuestion extends QuestionVariant {

}
