package com.example.ruleengine.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ast_nodes")
@Getter
@Setter
public class Node {

	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
	public String type; // "operator" for AND/OR, "operand" for conditions

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "left_child_id")
    private Node left;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "right_child_id")
    private Node right;

    @Column(nullable = true)
	public String value;

	public Node(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public Node(String type, Node left, Node right) {
        this.type = type;
        this.left = left;
        this.right = right;
    }
	public Node() {
	}

	public void addChild(Node child) {
        if (this.left == null) {
            this.left = child;
        } else if (this.right == null) {
            this.right = child; 
        } else {
            throw new IllegalStateException("Cannot add more than two children to a Node");
        }
    }
	public Node(String type, String value, Node left, Node right) {
		
		this.type = type;
		this.value = value;
		this.left = left;
		this.right = right;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Node getLeft() {
		return left;
	}

	public void setLeft(Node left) {
		this.left = left;
	}

	public Node getRight() {
		return right;
	}

	public void setRight(Node right) {
		this.right = right;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Node [id=" + id + ", type=" + type + ", left=" + left + ", right=" + right + ", value=" + value + "]";
	}

	
}
