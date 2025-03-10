/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Dashboard, TimeSettings } from '@streampipes/platform-services';

@Component({
    selector: 'sp-data-explorer-dashboard-toolbar',
    templateUrl: './dashboard-toolbar.component.html',
    styleUrls: ['./dashboard-toolbar.component.scss'],
})
export class DataExplorerDashboardToolbarComponent {
    @Input()
    dashboard: Dashboard;

    @Input()
    editMode: boolean;

    @Input()
    viewMode: string;

    @Input()
    timeRangeVisible: boolean;

    @Input()
    hasDataExplorerWritePrivileges: boolean;

    @Input()
    timeSettings: TimeSettings;

    @Output()
    viewModeChange: EventEmitter<string> = new EventEmitter<string>();

    @Output()
    saveDashboardEmitter: EventEmitter<void> = new EventEmitter();

    @Output()
    discardDashboardEmitter: EventEmitter<void> = new EventEmitter();

    @Output()
    deleteDashboardEmitter: EventEmitter<void> = new EventEmitter();

    @Output()
    triggerEditModeEmitter: EventEmitter<void> = new EventEmitter<void>();

    @Output()
    updateDateRangeEmitter: EventEmitter<TimeSettings> = new EventEmitter();

    @Output()
    intervalSettingsChangedEmitter: EventEmitter<void> =
        new EventEmitter<void>();
}
