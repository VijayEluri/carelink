/*     */ package minimed.ddms.applet.dtw.wizard.steps;
/*     */ 
/*     */ import java.util.ResourceBundle;
/*     */ import minimed.ddms.applet.dtw.wizard.Wizard;
/*     */ 
/*     */ public class MeterOperationFailStep extends DeviceOperationFailStep
/*     */ {
/*  37 */   private final String[] m_errorMessages = { this.m_resources.getString("wizard.opfail.meter.error0"), this.m_resources.getString("wizard.opfail.meter.error1"), this.m_resources.getString("wizard.opfail.meter.error2"), this.m_resources.getString("wizard.opfail.meter.error3"), this.m_resources.getString("wizard.opfail.meter.error4"), this.m_resources.getString("wizard.opfail.meter.error5"), this.m_resources.getString("wizard.opfail.meter.error6"), this.m_resources.getString("wizard.opfail.meter.error7"), this.m_resources.getString("wizard.opfail.meter.error8"), this.m_resources.getString("wizard.opfail.meter.error9"), this.m_resources.getString("wizard.opfail.meter.error10") };
/*     */ 
/*  82 */   private final String[] m_suggestionMessages = { this.m_resources.getString("wizard.opfail.meter.suggestion0"), this.m_resources.getString("wizard.opfail.meter.suggestion1"), this.m_resources.getString("wizard.opfail.meter.suggestion2"), this.m_resources.getString("wizard.opfail.meter.suggestion3"), this.m_resources.getString("wizard.opfail.meter.suggestion4"), this.m_resources.getString("wizard.opfail.meter.suggestion5"), this.m_resources.getString("wizard.opfail.meter.suggestion6"), this.m_resources.getString("wizard.opfail.meter.suggestion7"), this.m_resources.getString("wizard.opfail.meter.suggestion8"), this.m_resources.getString("wizard.opfail.meter.suggestion9"), this.m_resources.getString("wizard.opfail.meter.suggestion10") };
/*     */ 
/*     */   public MeterOperationFailStep(Wizard paramWizard)
/*     */   {
/* 131 */     super(paramWizard, "wizard.readfail.icon", null, "wizard.operation.read.title");
/*     */   }
/*     */ 
/*     */   protected String[] getErrorMessages()
/*     */   {
/* 143 */     return this.m_errorMessages;
/*     */   }
/*     */ 
/*     */   protected String[] getSuggestionMessages()
/*     */   {
/* 153 */     return this.m_suggestionMessages;
/*     */   }
/*     */ }

/* Location:           /home/bewest/Documents/bb/carelink/ddmsDTWApplet.jar
 * Qualified Name:     minimed.ddms.applet.dtw.wizard.steps.MeterOperationFailStep
 * JD-Core Version:    0.6.0
 */