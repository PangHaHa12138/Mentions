package com.bobomee.android.mentions.edit.util;

import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import com.bobomee.android.mentions.edit.MentionEditText;
import com.bobomee.android.mentions.model.Range;
import com.bobomee.android.mentions.listener.manager.RangeListenerManager;

/**
 * Project ID：400YF17051<br/>
 * Resume:
 *
 * @author 汪波
 * @version 1.0
 * @see
 * @since 2017/4/2 汪波 first commit
 */
public //handle the deletion action for mention string, such as '@test'
class HackInputConnection extends InputConnectionWrapper {
  private final MentionEditText editText;
  private final RangeListenerManager mRangeListenerManager;

  public HackInputConnection(InputConnection target, boolean mutable, MentionEditText editText) {
    super(target, mutable);
    this.editText = editText;
    mRangeListenerManager = new RangeListenerManager(editText);
  }

  @Override public boolean sendKeyEvent(KeyEvent event) {
    if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
      if (null != mRangeListenerManager) {
        int selectionStart = editText.getSelectionStart();
        int selectionEnd = editText.getSelectionEnd();
        Range closestRange =
            mRangeListenerManager.getRangeOfClosestMentionString(selectionStart, selectionEnd);
        if (closestRange == null) {
          editText.setSelected(false);
          return super.sendKeyEvent(event);
        }
        //if mention string has been selected or the cursor is at the beginning of mention string, just use default action(delete)
        if (editText.isSelected() || selectionStart == closestRange.getFrom()) {
          editText.setSelected(false);
          return super.sendKeyEvent(event);
        } else {
          //select the mention string
          editText.setSelected(true);
          editText.setLastSelectedRange(closestRange);
          setSelection(closestRange.getTo(), closestRange.getFrom());
        }
        return true;
      }
    }
    return super.sendKeyEvent(event);
  }

  @Override public boolean deleteSurroundingText(int beforeLength, int afterLength) {
    if (beforeLength == 1 && afterLength == 0) {
      return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)) && sendKeyEvent(
          new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
    }
    return super.deleteSurroundingText(beforeLength, afterLength);
  }

  public RangeListenerManager getRangeListenerManager() {
    return mRangeListenerManager;
  }
}
