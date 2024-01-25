package clientside;

import java.io.Serializable;

public class UpdateRequest implements Serializable {
    private final ClientGUI.Product updatedProduct;

    public UpdateRequest(ClientGUI.Product updatedProduct) {
        this.updatedProduct = updatedProduct;
    }

    public ClientGUI.Product getUpdatedProduct() {
        return updatedProduct;
    }
}
