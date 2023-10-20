package org.joget.marketplace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.tika.Tika;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormValidator;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.FileManager;
import org.joget.commons.util.LogUtil;

public class FileTypeValidator extends FormValidator {
    
    private final static String MESSAGE_PATH = "messages/FileTypeValidator";

    @Override
    public boolean validate(Element element, FormData data, String[] values) {
        boolean result = true;
        String id = FormUtil.getElementParameterName(element);

        String mandatory = (String) getProperty("mandatory");
        String message = (String) getProperty("message");
        Object fields = getProperty("fields");

        if ("true".equals(mandatory)) {
            result = validateMandatory(data, id, values, AppPluginUtil.getMessage("org.joget.marketplace.filetype.validator.empty", getClassName(), MESSAGE_PATH));
        }

        if (result) {
            List<String> mimeTypeList = new ArrayList<>();
            if (fields != null) {
                for (Object colObj : (Object[]) fields) {
                    Map col = (Map) colObj;
                    String fileType = (String) col.get("name");
                    String mimeType = (String) col.get("type");
                    mimeTypeList.add(mimeType);
                }
                String[] mimeTypeArray = mimeTypeList.toArray(new String[mimeTypeList.size()]);
                if (values != null && values.length > 0) {
                    for (String value : values) {
                        File file = FileManager.getFileByPath(value);
                        if (file != null && file.exists()) {
                            result = isAllowedFileType(file, mimeTypeArray);
                            if (!result) {
                                if (message == null || message.isEmpty()) {
                                    message = AppPluginUtil.getMessage("org.joget.marketplace.filetype.validator.invalid", getClassName(), MESSAGE_PATH);
                                }
                                data.addFormError(id, message);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public boolean isAllowedFileType(File file, String[] allowedMimeTypes) {
        Tika tika = new Tika();
        try {
            String detectedMimeType = tika.detect(file);
            for (String allowedMimeType : allowedMimeTypes) {
                if (allowedMimeType.equals(detectedMimeType)) {
                    return true;
                }
            }
        } catch (IOException e) {
            LogUtil.error(getClassName(), e, e.getMessage());
        }
        return false;
    }

    protected boolean validateMandatory(FormData data, String id, String[] values, String message) {
        boolean result = true;
        if (values == null || values.length == 0) {
            result = false;
            if (id != null) {
                data.addFormError(id, message);
            }
        } else {
            for (String val : values) {
                if (val == null || val.trim().length() == 0) {
                    result = false;
                    data.addFormError(id, message);
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return "File Type Validator";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getDescription() {
        return "To validate uploaded files based on the configured list of allowed file types and their associated MIME types.";
    }

    @Override
    public String getLabel() {
        return "File Type Validator";
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/fileTypeValidator.json", null, true, MESSAGE_PATH);
    }

}
