package org.slack_task_train.core.ifaces;

import com.slack.api.model.block.LayoutBlock;

public interface ISection {
    boolean constructCondition();
    LayoutBlock construct();
    IFormField getAccessory();
    String getId();
}
