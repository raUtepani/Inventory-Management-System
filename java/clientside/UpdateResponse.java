package clientside;

import java.io.Serializable;

public class UpdateResponse implements Serializable {
    private boolean updateSuccessful;
    private ClientGUI.Product updatedProduct;

    public UpdateResponse(boolean updateSuccessful, ClientGUI.Product updatedProduct) {
        this.updateSuccessful = updateSuccessful;
        this.updatedProduct = updatedProduct;
    }

    public boolean isUpdateSuccessful() {
        return updateSuccessful;
    }

    public ClientGUI.Product getUpdatedProduct() {
        return updatedProduct;
    }
}