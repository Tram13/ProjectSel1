package be.ugent.mydigipill.confirmationAlerts

import be.ugent.mydigipill.R

/**
 * This class is an exact implementation of the AbstractConfirmation class
 * @see AbstractConfirmation
 * This class uses the confirmation_text_fragment layout
 * @see R.layout.confirmation_text_fragment
 * This layout has 2 text buttons which can be set with the setButtonSkipText
 * @author Arthur Deruytter
 */
open class ConfirmationText : AbstractConfirmation(R.layout.confirmation_text_fragment)